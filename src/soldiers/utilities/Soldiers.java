package soldiers.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.database.SoldiersNamespaceContext;
import soldiers.search.Candidate;

public class Soldiers {

	static XPathFactory factory = XPathFactory.newInstance();
    static XPath xpath = factory.newXPath();

    public static void main(String[] args) throws TransformerException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, ParseException {

    	if ( args.length < 1 ) {
    		
    		System.err.println("Usage: Soldiers <filename>");
    		System.exit(1);
    	}
    	
    	String inputfile = args[0];
    	
		Document doc = readDocument(new FileInputStream(inputfile));			 
		doc.normalizeDocument();
		
		Connection connection = ConnectionManager.getConnection();
		
		//identifyPersonMentionsInPlaceXML(doc, connection);
		identifyPersonMentionsInPlaceXMLTest(doc, connection);
		
		TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new FileOutputStream("output/out.xml"));	
		transformer.transform(source, result);	
	}
        
	public static Document readDocument(InputStream input) throws ParserConfigurationException, SAXException, IOException {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(input);
		return doc;
	}

	public static void writeDocument(Document doc, OutputStream output) throws TransformerException {
		
		TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        //transformer.setOutputProperty(OutputKeys.METHOD, "xhtml");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(output);	
		transformer.setOutputProperty(OutputKeys.ENCODING, "windows-1252");
		transformer.transform(source, result);	
	}

	public static void identifyPersonMentionsInPlaceXML(Document doc, Connection connection) throws XPathExpressionException, TransformerConfigurationException, FileNotFoundException, SAXException, ParseException {
		
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
		
		xpath.setNamespaceContext(namespaceContext);
		XPathExpression expr = xpath.compile(".//soldiers:person");
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
    	
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Element e = (Element) list.item(i);
			Person person = parsePerson(e);
			
			// remove any results from a previous run
			XPathExpression prevcandidate = xpath.compile(".//soldiers:candidate");
			NodeList prevlist = (NodeList) prevcandidate.evaluate(e, XPathConstants.NODESET);
			
			for ( int c = 0; c < prevlist.getLength(); c++ ) {
				
				e.removeChild(prevlist.item(c));
				
			}

			
			System.out.println(person.getContent());
			List<Person> candidates = SearchSoldier.checkIdentity(person, connection);
			
			//AttributesImpl attr= new AttributesImpl();
			//attr.addAttribute("", "hits",  "hits", "Integer", String.valueOf(candidates.size()));
			
			for (Person p: candidates) {
			
				Set<Service> service = p.getService();
				Service svc = service.iterator().next();

				Element candidate = doc.createElement("candidate");
				candidate.setAttribute("sid", String.format("%d", p.getSoldierId()));
				candidate.setAttribute("content", p.getContent());
				candidate.setAttribute("sort", p.getSort());
				if ( svc.getNumber().length() > 0 ) candidate.setAttribute("number", svc.getNumber());
				candidate.setAttribute("rank", svc.getRank());
				e.appendChild(candidate);
				System.out.println("=" + p.getContent());
			}
			
			System.out.println("----------------");
		}
		
	}

	public static void identifyPersonMentionsInPlaceXMLTest(Document doc, Connection connection) throws XPathExpressionException, TransformerConfigurationException, FileNotFoundException, SAXException, ParseException {
		
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
		
		xpath.setNamespaceContext(namespaceContext);
		XPathExpression expr = xpath.compile(".//soldiers:person");
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
    	
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Element e = (Element) list.item(i);
			Person person = parsePerson(e);
			
			// remove any results from a previous run
			XPathExpression prevcandidate = xpath.compile(".//soldiers:candidate");
			NodeList prevlist = (NodeList) prevcandidate.evaluate(e, XPathConstants.NODESET);
			
			for ( int c = 0; c < prevlist.getLength(); c++ ) {
				
				e.removeChild(prevlist.item(c));
				
			}

			
			System.out.println(person.getContent());
			List<Candidate> candidates = SearchSoldier.findMatches(person, connection);
			
			int bestScore = Integer.MAX_VALUE;
			Iterator<Candidate> iter = candidates.iterator();
			
			while ( iter.hasNext() ) {
				
				Candidate c = iter.next();
				Person p = c.getPerson();
				
				if ( c.getScore().getOverallScore() <= bestScore ) {
					
					Service svc = p.getService().iterator().next();

					Element candidate = doc.createElement("candidate");
					candidate.setAttribute("sid", String.format("%d", p.getSoldierId()));
					candidate.setAttribute("content", p.getContent());
					candidate.setAttribute("sort", p.getSort());
					if ( svc.getNumber().length() > 0 ) candidate.setAttribute("number", svc.getNumber());
					candidate.setAttribute("rank", svc.getRank());
					candidate.setAttribute("regiment", svc.getRegiment());
					candidate.setAttribute("score", String.valueOf(c.getScore().getOverallScore()));
					e.appendChild(candidate);

					bestScore = c.getScore().getOverallScore();
				}
				else break;
				
			}
			
			System.out.println("----------------");
		}
		
	}

	public static Person parsePerson(Element element) throws ParseException {
		
		Person person = new Person();
		
		String sid = element.getAttribute("sid");
		if ( sid != null && sid.length() > 0 ) person.setSoldierId(Long.valueOf(sid));
		
		String surname   = element.getElementsByTagName("surname").getLength()   == 0 ? null : element.getElementsByTagName("surname").item(0).getTextContent();
		String forenames = element.getElementsByTagName("forenames").getLength() == 0 ? null : element.getElementsByTagName("forenames").item(0).getTextContent();
		String initials  = element.getElementsByTagName("initials").getLength()  == 0 ? null : element.getElementsByTagName("initials").item(0).getTextContent();
		String suffix    = element.getElementsByTagName("suffix").getLength()    == 0 ? null : element.getElementsByTagName("suffix").item(0).getTextContent();

		person.setSurname(surname);
		person.setInitials(initials);
		person.setForenames(forenames);
		person.setSuffix(suffix);
		
		NodeList nl = element.getElementsByTagName("death");
		
		if ( nl.getLength() > 0 ) {
			
			Element e = (Element) nl.item(0);
			String dd = e.getAttribute("date");
			String da = e.getAttribute("after");
			String db = e.getAttribute("before");
			
			if ( dd.length() > 0 )  person.setDeath(Date.valueOf(dd));
			if ( da.length() > 0 )  person.setDiedafter(Date.valueOf(da));
			if ( db.length() > 0 )  person.setDiedbefore(Date.valueOf(db));
		}
		
		
		nl = element.getElementsByTagName("birth");
		
		if ( nl.getLength() > 0 ) {
			
			Element e = (Element) nl.item(0);
			String bd = e.getAttribute("date");
			String ba = e.getAttribute("after");
			String bb = e.getAttribute("before");
			
			if ( bd.length() > 0 )  person.setBirth(Date.valueOf(bd));
			if ( ba.length() > 0 )  person.setBornafter(Date.valueOf(ba));
			if ( bb.length() > 0 )  person.setBornbefore(Date.valueOf(bb));
		}
		
 		Element service = (Element) element.getElementsByTagName("service").item(0);
		
		if ( service != null ) {
			
			NodeList records = service.getElementsByTagName("record");
			
			for ( int i = 0; i < records.getLength(); i++ ) {
				
				Element record = (Element) records.item(i);
				String number = record.getAttribute("number");
				String rank   = record.getAttribute("rank");
				String regmt  = record.getAttribute("regiment");
				String unit   = record.getAttribute("unit");
				String after  = record.getAttribute("after");
				String before = record.getAttribute("before");

				Service serviceRecord = new Service();
				if ( number.length() > 0 )  serviceRecord.setNumber(number);
				if ( rank.length() > 0 )    serviceRecord.setRank(rank);
				if ( regmt.length() > 0 )   serviceRecord.setRegiment(regmt);
				if ( unit.length() > 0 )    serviceRecord.setUnit(unit);
				if ( after.length() > 0 )   serviceRecord.setAfter(Date.valueOf(after));
				if ( before.length() > 0 )  serviceRecord.setBefore(Date.valueOf(before));

				serviceRecord.setSoldierId(person.getSoldierId());
				person.addService(serviceRecord);
			}
		}

		return person;
	}

}
