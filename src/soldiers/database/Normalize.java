package soldiers.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soldiers.utilities.Soldiers;
import soldiers.utilities.XmlUtils;

/**
 * This utility normalizes values in Soldiers XML. Currently this is just the
 * rank attributes on service elements.
 * 
 * @author The Royal Hampshire Regiment Museum
 *
 */

public class Normalize {

	public static void main(String[] args) throws XPathExpressionException, IllegalArgumentException,
			FileNotFoundException, SAXException, ParseException, TransformerException {

		if (args.length < 2) {

			System.err.println("Usage: Normalize <input filename> <output filename>");
			System.exit(1);
		}

		String inputfile = args[0];
		String outputfile = args[1];

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		XmlUtils xmlutils = new XmlUtils();
		Document doc = xmlutils.parse(new File(inputfile));

		NamespaceContext namespaceContext = new SoldiersNamespaceContext();

		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
		TransformerHandler serializer;

		xpath.setNamespaceContext(namespaceContext);
		XPathExpression expr = xpath.compile(".//soldiers:person");
		XPathExpression notesexpr = xpath.compile(".//soldiers:note");
		XPathExpression befores = xpath.compile(".//soldiers:*[@before|@after]");
		XPathExpression desc = xpath.compile(".//soldiers:description[1]");

		Element description = (Element) desc.evaluate(doc.getDocumentElement(), XPathConstants.NODE);

		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);

		Normalize normalizer = new Normalize();

		Map<String, String> ranks = normalizer.getRanks();

		Document collectedResults = xmlutils.newDocument();
		Element collection = (Element) collectedResults
				.appendChild(collectedResults.createElementNS(namespaceContext.getNamespaceURI("soldiers"), "list"));
		
		if ( description != null ) {
			collection.appendChild(collectedResults.importNode(description, true));
		}
		
		System.out.println("Number of persons: " + list.getLength());

		for (int i = 0; i < list.getLength(); i++) {
			
			if (i % 100 == 0 ) System.out.println(i);

			Element e = (Element) list.item(i);

			// check dates
			NodeList dlist = e.getElementsByTagName("death");

			if (dlist.getLength() == 1) {

				Element delem = (Element) dlist.item(0);
				checkDate(delem);
			}

			dlist = e.getElementsByTagName("birth");

			if (dlist.getLength() == 1) {

				Element delem = (Element) dlist.item(0);
				checkDate(delem);
			}

			// Normalize "before" and "after" dates - if just year or month, then these
			// become the first/last day of the
			// year or month, as appropriate.

			NodeList beforelist = (NodeList) befores.evaluate(e, XPathConstants.NODESET);

			for (int j = 0; j < beforelist.getLength(); j++) {

				Element parent = (Element) beforelist.item(j);
				String value = parent.getAttribute("before");

				if (value.length() > 0) {

					parent.setAttribute("before", before(normalizeDate(value)));
				}

				value = parent.getAttribute("after");

				if (value.length() > 0) {

					parent.setAttribute("after", after(normalizeDate(value)));
				}
			}
			
			// check that initials make sense

			NodeList ilist = e.getElementsByTagName("initials");

			if (ilist.getLength() == 1) {

				Element ielem = (Element) ilist.item(0);
				checkInitials(ielem.getTextContent());
			}

			// Parse the person XML. This will force names to upper case, and determine
			// initials from forenames if only forenames
			// are in the XML.
			Person p = Soldiers.parsePerson(e);

			// Normalize ranks: The database will accept any text value for rank, but we
			// must be consistent if we want to search by rank.
			// This step converts any expression of rank to the relevant form given in the
			// "RANK" table (see ranks.csv).
			normalizer.normalizeRank(p, ranks);

			// Validate
			validate(p);

			// Collect output: Serialize each person into an XML DOM (picking up corrections
			// made in parsing and normalization),
			// and add in the "notes" elements (which aren't parsed or normalized). Add each
			// person to the collected results XML.
			Document results = xmlutils.newDocument();
			serializer = tf.newTransformerHandler();
			serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			serializer.setResult(new DOMResult(results));

			serializer.startDocument();
			p.serializePerson(serializer);
			serializer.endDocument();

			Element newp = (Element) results.getDocumentElement();

			NodeList notes = (NodeList) notesexpr.evaluate(e, XPathConstants.NODESET);

			for (int j = 0; j < notes.getLength(); j++) {

				Node newNode = results.importNode(notes.item(j), true);
				newp.appendChild(newNode);
			}

			Node importNode = collectedResults.importNode(results.getDocumentElement(), true);
			collection.appendChild(importNode);
		}

		// Output the results
		TransformerFactory treansformerFactory = TransformerFactory.newInstance();
		Transformer transformer = treansformerFactory.newTransformer();
		StreamResult result = new StreamResult(new FileOutputStream(outputfile));

		DOMSource source = new DOMSource(collectedResults);
		transformer.transform(source, result);

		System.out.println("finished");
	}

	public Map<String, String> getRanks() { // read this from a file instead?

		Map<String, String> ranks = new HashMap<String, String>();

		ranks.put("boy", "Boy");
		ranks.put("hosapp", "Hos App");
		ranks.put("hospitalapprentice", "Hos App");
		ranks.put("pte", "Pte");
		ranks.put("pt", "Pte");
		ranks.put("priva", "Pte");
		ranks.put("pnr", "Pnr");
		ranks.put("pioneer", "Pnr");
		ranks.put("sig", "Sig");
		ranks.put("bgr", "Bgr");
		ranks.put("bugler", "Bgr");
		ranks.put("trumpeter", "Pte");
		ranks.put("trooper", "Tpr");
		ranks.put("tpr", "Tpr");
		ranks.put("private", "Pte");
		ranks.put("driver", "Pte");
		ranks.put("cyclist", "Pte");
		ranks.put("privatepioneer", "Pte");
		ranks.put("privateactingcorporal", "Pte");
		ranks.put("privatelancecorporal", "Pte");
		ranks.put("privateserjeant", "Pte");
		ranks.put("dmr", "Dmr");
		ranks.put("drm", "Dmr");
		ranks.put("bandsman", "Bdsm");
		ranks.put("bdmn", "Bdsm");
		ranks.put("drumr", "Dmr");
		ranks.put("dvr", "Dvr");
		ranks.put("gnr", "Gnr");
		ranks.put("gunner", "Gnr");
		ranks.put("signaller", "Sig");
		ranks.put("sapper", "Spr");
		ranks.put("spr", "Spr");
		ranks.put("rfn", "Rfn");
		ranks.put("rifleman", "Rfn");
		ranks.put("bdsm", "Bdsm");
		ranks.put("drummer", "Dmr");
		ranks.put("bombadier", "Bdr");
		ranks.put("bdr", "Bdr");
		ranks.put("lbombadier", "L/Bdr");
		ranks.put("lbdr", "L/Bdr");
		ranks.put("lcpl", "L/Cpl");
		ranks.put("lc", "L/Cpl");
		ranks.put("lcorpl", "L/Cpl");
		ranks.put("lancecorp", "L/Cpl");
		ranks.put("lancecorpl", "L/Cpl");
		ranks.put("lancecorporal", "L/Cpl");
		ranks.put("corporal2nd", "2Cpl");
		ranks.put("2ndcorporal", "2Cpl");
		ranks.put("cpl", "Cpl");
		ranks.put("acpl", "Cpl");
		ranks.put("actcpl", "Cpl");
		ranks.put("actcorpl", "Cpl");
		ranks.put("corporal", "Cpl");
		ranks.put("corp", "Cpl");
		ranks.put("corpl", "Cpl");
		ranks.put("corporalactingstaffserjeantmajor", "Cpl");
		ranks.put("corporalactinglanceserjeant", "Cpl");
		ranks.put("corporalofhorse", "Cpl");
		ranks.put("lsgt", "L/Sgt");
		ranks.put("lsergt", "L/Sgt");
		ranks.put("lsjt", "L/Sgt");
		ranks.put("lancesergeant", "L/Sgt");
		ranks.put("lanceserjeant", "L/Sgt");
		ranks.put("corporallanceserjeant", "L/Sgt");
		ranks.put("lancesgt", "L/Sgt");
		ranks.put("lancesjt", "L/Sgt");
		ranks.put("2ndsergeant", "L/Sgt");
		ranks.put("sgt", "Sgt");
		ranks.put("actsgt", "Sgt");
		ranks.put("sjt", "Sgt");
		ranks.put("sergt", "Sgt");
		ranks.put("serjeant", "Sgt");
		ranks.put("sergeant", "Sgt");
		ranks.put("sergent", "Sgt");
		ranks.put("armsgt", "Sgt");
		ranks.put("paysgt", "Sgt");
		ranks.put("hossgt", "Sgt");
		ranks.put("hospsgt", "Sgt");
		ranks.put("orsgt", "Sgt");
		ranks.put("musksgt", "Sgt");
		ranks.put("sgtdrm", "Sgt");
		ranks.put("sgtdmr", "Sgt");
		ranks.put("sergeantdrummer", "Sgt");
		ranks.put("drmsgt", "Sgt");
		ranks.put("orderlyroomsergeant", "Sgt");
		ranks.put("armourersergeant", "Sgt");
		ranks.put("armrserjeant", "Sgt");
		ranks.put("nowcommissionedserjeant", "Sgt");
		ranks.put("serjeanttrumpeter", "Sgt");
		ranks.put("serjeantcook", "Sgt");
		ranks.put("sgtdrummer", "Sgt");
		ranks.put("bandsjt", "Sgt");
		ranks.put("bandsgt", "Sgt");
		ranks.put("actsergt", "Sgt");
		ranks.put("bandmaster", "Sgt");
		ranks.put("farriersgt", "Sgt");
		ranks.put("farrierserjeant", "Sgt");
		ranks.put("serjeantactingcompanyserjeantmajor", "Sgt");
		ranks.put("qrmrsgt", "QMS");
		ranks.put("quartmastersergeant", "QMS");
		ranks.put("farrierquartermasterserjeant", "QMS");
		ranks.put("squadronquartermasterserjeant", "QMS");
		ranks.put("quartermastersergeant", "QMS");
		ranks.put("qmsgt", "QMS");
		ranks.put("csgt", "CSgt");
		ranks.put("colsgt", "CSgt");
		ranks.put("csjt", "CSgt");
		ranks.put("bmstr", "CSgt");
		ranks.put("b'mstr", "CSgt");
		ranks.put("bm", "CSgt");
		ranks.put("coloursergeant", "CSgt");
		ranks.put("colourserjeant", "CSgt");
		ranks.put("crsgt", "CSgt");
		ranks.put("dm", "CSgt");
		ranks.put("ssgt", "SSgt");
		ranks.put("ssgt", "SSgt");
		ranks.put("staffsergeant", "SSgt");
		ranks.put("staffserjeant", "SSgt");
		ranks.put("saddlerserjeant", "SSgt");
		ranks.put("fitterstaffserjeant", "SSgt");
		ranks.put("wheelerstaffserjeant", "SSgt");
		ranks.put("sgtmt", "SSgt");
		ranks.put("sasgt", "SSgt");
		ranks.put("wo2", "WO2");
		ranks.put("woclii", "WO2");
		ranks.put("warrantofficerclassii", "WO2");
		ranks.put("warrantofficerclass2", "WO2");
		ranks.put("woclass2", "WO2");
		ranks.put("wocl2", "WO2");
		ranks.put("wo1", "WO1");
		ranks.put("woclass1", "WO1");
		ranks.put("warrantofficerclass1", "WO1");
		ranks.put("warrantofficer1stclass", "WO1");
		ranks.put("sergeantmajor", "Sgt Maj");
		ranks.put("sergtmajor", "Sgt Maj");
		ranks.put("serjeantmajor", "Sgt Maj");
		ranks.put("squadronserjeantmajor", "Sgt Maj");
		ranks.put("staffserjeantmajor", "Sgt Maj");
		ranks.put("sgtmaj", "Sgt Maj");
		ranks.put("sergtmaj", "Sgt Maj");
		ranks.put("sergtmajor", "Sgt Maj");
		ranks.put("smjr", "Sgt Maj");
		ranks.put("sm", "Sgt Maj");
		ranks.put("rsm", "RSM");
		ranks.put("regimentalsergeantmajor", "RSM");
		ranks.put("regimentalserjeantmajor", "RSM");
		ranks.put("drmmaj", "Drum Maj");
		ranks.put("drummaj", "Drum Maj");
		ranks.put("cqms", "CQMS");
		ranks.put("companyquartermasterserjeant", "CQMS");
		ranks.put("companyquartermastersergeant", "CQMS");
		ranks.put("companyquartermastersergt", "CQMS");
		ranks.put("qms", "QMS");
		ranks.put("quartermasterserjeant", "QMS");
		ranks.put("staffquartermasterserjeant", "QMS");
		ranks.put("qmrsgt", "QMS");
		ranks.put("sqms", "QMS");
		ranks.put("rqms", "RQMS");
		ranks.put("regimentalquartermastersergeant", "RQMS");
		ranks.put("regimentalquartermasterserjeant", "RQMS");
		ranks.put("coloursergeantmajor", "CSM");
		ranks.put("csm", "CSM");
		ranks.put("csmjr", "CSM");
		ranks.put("companysergeantmajor", "CSM");
		ranks.put("companyserjeantmajor", "CSM");
		ranks.put("cosergtmajor", "CSM");
		ranks.put("companyquartermastersergeantmajor", "CSM");
		ranks.put("cadet", "Cadet");
		ranks.put("officercadet", "Cadet");
		ranks.put("ens", "Ens");
		ranks.put("ensign", "Ens");
		ranks.put("2lt", "2Lt");
		ranks.put("2lieut", "2Lt");
		ranks.put("seclieut", "2Lt");
		ranks.put("2ndlt", "2Lt");
		ranks.put("2ndlieut", "2Lt");
		ranks.put("secondlieutenant", "2Lt");
		ranks.put("secondlieut", "2Lt");
		ranks.put("secondlieutenanttemporarylieutenant", "2Lt");
		ranks.put("2ndlieutenant", "2Lt");
		ranks.put("lt", "Lt");
		ranks.put("lieut", "Lt");
		ranks.put("lieutenant", "Lt");
		ranks.put("quartermasterandlieutenant", "Lt");
		ranks.put("paylieutenant", "Lt");
		ranks.put("asstsurg", "Asst Surg");
		ranks.put("astsurg", "Asst Surg");
		ranks.put("subaltern", "Sub");
		ranks.put("sub", "Sub");
		ranks.put("capt", "Capt");
		ranks.put("captain", "Capt");
		ranks.put("quartermasterandcaptain", "Capt");
		ranks.put("captaintemporarylieutenantcolonel", "Capt");
		ranks.put("adjt", "Adjt");
		ranks.put("adjutant", "Adjt");
		ranks.put("adj", "Adjt");
		ranks.put("qmr", "QM");
		ranks.put("quartermastercaptain", "QM");
		ranks.put("qm", "QM");
		ranks.put("quartermaster", "QM");
		ranks.put("maj", "Maj");
		ranks.put("major", "Maj");
		ranks.put("surg", "Surg");
		ranks.put("surgeon", "Surg");
		ranks.put("ltcol", "Lt Col");
		ranks.put("ltcolonel", "Lt Col");
		ranks.put("lieutenantcolonel", "Lt Col");
		ranks.put("lieutcol", "Lt Col");
		ranks.put("lieutcolonel", "Lt Col");
		ranks.put("temporarylieutcolonel", "Lt Col");
		ranks.put("brevetlieutcolonel", "Lt Col");
		ranks.put("col", "Col");
		ranks.put("btcol", "Col");
		ranks.put("brevetcolonel", "Col");
		ranks.put("honorarycoloneltemporarybrigadiergeneral", "Col");
		ranks.put("colonel", "Col");
		ranks.put("brigadier", "Brig");
		ranks.put("brig", "Brig");
		ranks.put("majorgeneral", "Maj Gen");
		ranks.put("majgeneral", "Maj Gen");
		ranks.put("majgen", "Maj Gen");
		ranks.put("briggen", "Brig Gen");
		ranks.put("brigadiergeneral", "Brig Gen");
		ranks.put("gen", "Gen");
		ranks.put("general", "Gen");
		ranks.put("paymstr", "Capt");
		ranks.put("civsurg", "UNK");
		ranks.put("unk", "UNK");
		ranks.put("unknown", "UNK");
		ranks.put("soldier", "UNK");
		ranks.put("chaplain", "UNK");
		ranks.put("superintendent", "UNK");
		ranks.put("smstr", "UNK");
		ranks.put("warranto", "UNK");
		ranks.put("captainflightcommander", "UNK");
		ranks.put("assistantpaymaster", "UNK");
		ranks.put("secondflightlieutenant", "UNK");
		ranks.put("flightlieutenant", "UNK");
		ranks.put("flyingofficer", "UNK");
		ranks.put("flyingsublieutenant", "UNK");
		ranks.put("ableseamanrnvr", "UNK");
		ranks.put("flightofficer", "UNK");
		ranks.put("fightsubsecondlieutenant", "UNK");

		return ranks;
	}

	public void normalizeRank(Service service, Map<String, String> ranks) {

		if (service.getRank() == null) {

			return;

		}

		String rankqualifier = null;
		String raw = service.getRank().toLowerCase().trim();

		if (raw.startsWith("a/")) {

			rankqualifier = "A";
			raw = raw.substring(2);
		}

		if (raw.startsWith("acting")) {

			rankqualifier = "A";
			raw = raw.substring(6);
		}

		if (raw.startsWith("t/")) {

			rankqualifier = "T";
			raw = raw.substring(2);
		}

		if (raw.startsWith("temporary")) {

			rankqualifier = "T";
			raw = raw.substring(9);
		}

		raw = raw.replaceAll("\\p{javaSpaceChar}", " ").trim();
		raw = raw.split("\\(")[0];
		raw = raw.split("&")[0];
		raw = raw.replaceAll("/", "");
		raw = raw.replaceAll("-", "");
		raw = raw.replaceAll("’", "");
		raw = raw.replaceAll("\\.", "");
		raw = raw.replaceAll("\\s+", "");
		String normal = ranks.get(raw);

		if (normal != null) {

			service.setRank(normal);
			service.setRankqualifier(rankqualifier);

		} else {

			System.out.println(service + " has no rank for: " + raw);
		}

	}

	public void normalizeRank(Person person, Map<String, String> ranks) {

		for (Service service : person.getService()) {

			normalizeRank(service, ranks);
		}

	}

	public static void normalizeRank(List<Person> list) {

		Normalize normalizer = new Normalize();
		Map<String, String> ranks = normalizer.getRanks();

		for (Person person : list) {

			normalizer.normalizeRank(person, ranks);
		}
	}

	public static String normalizeDate(String inputDate) {

//		DateTimeFormatter df1 = DateTimeFormatter.ofPattern("M/y");

		String outputDate = "";
		if (inputDate == null)
			return outputDate;
		String txt = inputDate.trim();
		if (txt.length() == 0)
			return outputDate;

		txt = txt.replaceAll("(\\.|,)\\s*", "\\/");

		SimpleDateFormat f1 = new SimpleDateFormat("d/M/y");
		SimpleDateFormat f2 = new SimpleDateFormat("d MMM y");
		SimpleDateFormat f3 = new SimpleDateFormat("MMM-y");
		SimpleDateFormat f4 = new SimpleDateFormat("y");
		SimpleDateFormat f5 = new SimpleDateFormat("M/y");
		SimpleDateFormat f6 = new SimpleDateFormat("MMM y");

		SimpleDateFormat o1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat o2 = new SimpleDateFormat("yyyy-MM");
		SimpleDateFormat o3 = new SimpleDateFormat("yyyy");

		try {

			if (txt.matches("^\\d{4}\\-\\d{2}\\-\\d{2}$")) { // normal form anyway

				outputDate = o1.format(o1.parse(txt));
			}
			else if (txt.matches("^\\w{3,4}\\-\\d+$")) {

				outputDate = o2.format(f3.parse(txt));
			}
			else if (txt.matches("^\\w{3}\\s\\d+$")) {

				outputDate = o2.format(f6.parse(txt));
			}
			else if (txt.matches("^\\d+\\/\\d+\\/\\d{4}$")) {

				outputDate = o1.format(f1.parse(txt));
			}
			else if (txt.matches("^\\d+\\/\\d+\\/\\d{2}$")) {

				outputDate = o1.format(f1.parse(txt));
			}
			else if (txt.matches("^\\d+\\s\\w{3}\\s\\d{4}$")) {

				outputDate = o1.format(f2.parse(txt));
			}
			else if (txt.matches("^\\d+\\/\\d{4}$")) {

				outputDate = o2.format(f5.parse(txt));
			}
			else if (txt.matches("^\\d{4}$")) {

				outputDate = o3.format(f4.parse(txt));
			}
			else {
				System.err.println("Can't parse: " + inputDate + "<" + txt + ">");
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return outputDate;
	}

	public static void checkDate(Element elem) {

		// Try and normalize the @date attribute (on birth or death element)

		// If the attribute is a month or year, rather than a specific date, then
		// replace the @date attribute with suitable
		// @before and @after attributes

		String rawdate = elem.getAttribute("date");
		String normal = normalizeDate(rawdate);

		if (normal.length() != 10 && normal.length() > 0) {

			elem.removeAttribute("date");
			elem.setAttribute("after", after(normal));
			elem.setAttribute("before", before(normal));
		} else {

			elem.setAttribute("date", normal);
		}
	}

	public static String after(String date) {

		if (date.length() == 4) {

			return date + "-01-01";
		} else if (date.length() == 7) {

			return date + "-01";
		} else if (date.length() == 10) {

			return date;
		} else {
			System.err.println("date is not normalized: " + date);
			return null;
		}
	}
	
	public static void checkInitials(String initials) {
		
		if ( initials.matches(".*[a-z].*") ) {
			
			// This might mean forenames in initials field ...
			System.err.println("Lowercase in intials: " + initials);
		}
	}

	public static String before(String date) {

		if (date.length() == 4) {

			return date + "-12-31";
		} else if (date.length() == 7) {

			YearMonth ym = YearMonth.parse(date);
			return String.format("%s-%2d", date, ym.lengthOfMonth());
		} else if (date.length() == 10) {

			return date;
		} else {
			System.err.println("date is not normalized: " + date);
			return null;
		}
	}

	public static void validate(Person p) {

		// There must be a surname

		if (p.getSurname() == null || p.getSurname().trim().length() == 0)
			System.err.println("no surname: " + p.getContent());
		
		// Dates
		
		Calendar now = Calendar.getInstance();		
		Calendar birth = null, death = null;
		
		if ( p.getBirth() != null ) {
			
			birth = Calendar.getInstance();
			birth.setTime(p.getBirth());
		}
		
		if ( p.getDeath() != null ) {
			
			death = Calendar.getInstance();
			death.setTime(p.getDeath());
		}
		
		if ( birth != null && birth.after(now ))  System.err.println("date of birth is in the future: " + p.getContent());
		
		if ( birth != null && death != null ) {
			
			if ( death.before(birth) ) System.err.println("date of death before date of birth: " + p.getContent());
			
			if ( death.get(Calendar.YEAR) - birth.get(Calendar.YEAR) > 100 )  System.err.println("lived more than 100 years? " + p.getContent());
		}

		// check service records ...

		for (Service s : p.getService()) {
			
			Calendar before = null, after = null;

			// The database requires that each service record must have a "before" attribute
			// (as it forms part of the primary key)

			if ( s.getBefore() != null ) {
						
				before = Calendar.getInstance();
				before.setTime(s.getBefore());
				
				if ( before.after(now ))  System.err.println("service record 'before' is in the future: " + p.getContent() + "; " + s);				
			}
			else {
				
				System.err.println("service record without 'before' attribute: " + p.getContent());
			}
			
			if ( s.getAfter() != null ) {
				
				after = Calendar.getInstance();
				after.setTime(s.getAfter());

				if ( after.after(now ))  System.err.println("service record 'after' is in the future: " + p.getContent() + "; " + s);
			}
			
			// the 'before' date must be later that the 'after' date (if specified)

			if (before != null && after != null) {

				if (before.compareTo(after) < 0)  System.err.println("service record 'before' should be later than 'after': " + p.getContent() + "; " + s);

				if ( death != null && before.compareTo(death) > 0 )  {
					s.setBefore(new java.sql.Date(death.getTimeInMillis()));
				}
				if ( birth != null && before.compareTo(birth) < 0 )  System.err.println("service record 'before' should be later than date of birth: " + p.getContent() + "; " + s);
			}		
		}
	}
	
}
