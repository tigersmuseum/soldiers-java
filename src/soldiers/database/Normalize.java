package soldiers.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
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
 * This utility normalizes values in Soldiers XML. Currently this is just the rank attributes on service elements.
 * 
 * @author The Royal Hampshire Regiment Museum
 *
 */

public class Normalize {

	public static void main(String[] args) throws XPathExpressionException, IllegalArgumentException, FileNotFoundException, SAXException, ParseException, TransformerException {

    	if ( args.length < 2 ) {
    		
    		System.err.println("Usage: Normalize <input filename> <output filename>");
    		System.exit(1);
    	}
    	
    	String inputfile  = args[0];
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
		
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		Normalize normalizer = new Normalize();

		Map<String, String> ranks = normalizer.getRanks();
		
		Document collectedResults = xmlutils.newDocument();
		Element collection = (Element) collectedResults.appendChild(collectedResults.createElementNS(namespaceContext.getNamespaceURI("soldiers"), "list"));
		
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Element e = (Element) list.item(i);
	        Person p = Soldiers.parsePerson(e);
	        normalizer.normalizeRank(p, ranks);

			Document results = xmlutils.newDocument();
	        serializer = tf.newTransformerHandler();
	        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	        serializer.setResult(new DOMResult(results));
	        
	        serializer.startDocument();
	        p.serializePerson(serializer);
	        serializer.endDocument();
	        
	        Element newp = (Element) results.getDocumentElement();
	        
			NodeList notes = (NodeList) notesexpr.evaluate(e, XPathConstants.NODESET);
			
			for ( int j = 0; j < notes.getLength(); j++ ) {
				
				Node newNode = results.importNode(notes.item(j), true);
				newp.appendChild(newNode);				
			}
			
			Node importNode = collectedResults.importNode(results.getDocumentElement(), true);
			collection.appendChild(importNode);
		}
		
		TransformerFactory treansformerFactory = TransformerFactory.newInstance();
		Transformer transformer = treansformerFactory.newTransformer();
		StreamResult result = new StreamResult(new FileOutputStream(outputfile));
		
		DOMSource source = new DOMSource(collectedResults);
		transformer.transform(source, result);      
	}
	
	
	public Map<String, String> getRanks() {
		
		Map<String, String> ranks = new HashMap<String, String>();
		
		ranks.put("boy", "Boy");
		ranks.put("hosapp", "Hos App");
		ranks.put("hospitalapprentice", "Hos App");
		ranks.put("pte", "Pte");
		ranks.put("pnr", "Pnr");
		ranks.put("pioneer", "Pnr");
		ranks.put("sig", "Sig");
		ranks.put("bgr", "Bgr");
		ranks.put("bugler", "Bgr");
		ranks.put("trooper", "Tpr");
		ranks.put("tpr", "Tpr");
		ranks.put("private", "Pte");
		ranks.put("privatepioneer", "Pte");
		ranks.put("dmr", "Dmr");
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
		ranks.put("lancecorporal", "L/Cpl");
		ranks.put("cpl", "Cpl");
		ranks.put("acpl", "Cpl");
		ranks.put("corporal", "Cpl");
		ranks.put("corp", "Cpl");
		ranks.put("lsgt", "L/Sgt");
		ranks.put("lsjt", "L/Sgt");
		ranks.put("lancesergeant", "L/Sgt");
		ranks.put("lanceserjeant", "L/Sgt");
		ranks.put("sgt", "Sgt");
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
		ranks.put("sgtdrummer", "Sgt");
		ranks.put("bandsjt", "Sgt");
		ranks.put("bandsgt", "Sgt");
		ranks.put("quartmastersergeant", "QMS");
		ranks.put("quartermastersergeant", "QMS");
		ranks.put("qmsgt", "QMS");
		ranks.put("csgt", "CSgt");
		ranks.put("csjt", "CSgt");
		ranks.put("bmstr", "CSgt");
		ranks.put("coloursergeant", "CSgt");
		ranks.put("colourserjeant", "CSgt");
		ranks.put("crsgt", "CSgt");
		ranks.put("ssgt", "SSgt");
		ranks.put("staffsergeant", "SSgt");
		ranks.put("staffserjeant", "SSgt");
		ranks.put("fitterstaffserjeant", "SSgt");
		ranks.put("wo2", "WO2");
		ranks.put("warrantofficerclassii", "WO2");
		ranks.put("warrantofficerclass2", "WO2");
		ranks.put("woclass2", "WO2");
		ranks.put("wocl2", "WO2");
		ranks.put("wo1", "WO1");
		ranks.put("woclass1", "WO1");
		ranks.put("warrantofficer1stclass", "WO1");
		ranks.put("sergeantmajor", "Sgt Maj");
		ranks.put("serjeantmajor", "Sgt Maj");
		ranks.put("sgtmaj", "Sgt Maj");
		ranks.put("smjr", "Sgt Maj");
		ranks.put("rsm", "RSM");
		ranks.put("regimentalsergeantmajor", "RSM");
		ranks.put("regimentalserjeantmajor", "RSM");
		ranks.put("drmmaj", "Drum Maj");
		ranks.put("drummaj", "Drum Maj");
		ranks.put("cqms", "CQMS");
		ranks.put("companyquartermasterserjeant", "CQMS");
		ranks.put("companyquartermastersergeant", "CQMS");
		ranks.put("qms", "QMS");
		ranks.put("quartermasterserjeant", "QMS");
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
		ranks.put("companyquartermastersergeantmajor", "CSM");
		ranks.put("cadet", "Cadet");
		ranks.put("officercadet", "Cadet");
		ranks.put("ens", "Ens");
		ranks.put("ensign", "Ens");
		ranks.put("2lt", "2Lt");
		ranks.put("2lieut", "2Lt");
		ranks.put("2ndlt", "2Lt");
		ranks.put("secondlieutenant", "2Lt");
		ranks.put("2ndlieutenant", "2Lt");
		ranks.put("lt", "Lt");
		ranks.put("lieut", "Lt");
		ranks.put("lieutenant", "Lt");
		ranks.put("asstsurg", "Asst Surg");
		ranks.put("astsurg", "Asst Surg");
		ranks.put("subaltern", "Sub");
		ranks.put("sub", "Sub");
		ranks.put("capt", "Capt");
		ranks.put("captain", "Capt");
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
		ranks.put("brevetlieutcolonel", "Lt Col");
		ranks.put("col", "Col");
		ranks.put("btcol", "Col");
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
		
		return ranks;
	}
	
	
	public void normalizeRank(Service service, Map<String, String> ranks) {
		
		if (service.getRank() == null) {
			
			//System.out.println("NO RANK: " + service);
			return;
			
		}
		
		String rankqualifier = null;
		String raw = service.getRank().toLowerCase().trim();
		
		if ( raw.startsWith("a/")) {
			
			rankqualifier = "A";
			raw = raw.substring(2);
		}

		if ( raw.startsWith("acting")) {
			
			rankqualifier = "A";
			raw = raw.substring(6);
		}
		
		if ( raw.startsWith("t/")) {
			
			rankqualifier = "T";
			raw = raw.substring(2);
		}
		
		raw = raw.replaceAll("\\p{javaSpaceChar}", " ").trim();
		raw = raw.split("\\(")[0];
		raw = raw.split("&")[0];
		raw = raw.replaceAll("/", "");
		raw = raw.replaceAll("-", "");
		raw = raw.replaceAll("\\.", "");
		raw = raw.replaceAll("\\s+", "");
		String normal = ranks.get(raw);
		
		if ( normal != null ) {
			
			service.setRank(normal);
			service.setRankqualifier(rankqualifier);
			
		}
		else {
			
			System.out.println(service + " has no rank for: " + raw);
		}
		
	}
	

	public void normalizeRank(Person person, Map<String, String> ranks) {
		
		for ( Service service: person.getService() ) {
			
			normalizeRank(service, ranks);			
		}
		
	}
	
	
	public static void normalizeRank(List<Person> list) {
		
		Normalize normalizer = new Normalize();
		Map<String, String> ranks = normalizer.getRanks();
		
		for ( Person person: list ) {
			
			normalizer.normalizeRank(person, ranks);
		}
	}

}
