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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.language.Metaphone;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.database.SoldiersNamespaceContext;
import soldiers.search.Candidate;
import soldiers.search.MakeEncoderMap;
import soldiers.search.PersonFinder;

/**
 * Identify person entries in a Soldiers XML file.
 * 
 * @author Royal Hampshire Regiment Museum
 *
 */

public class Soldiers {

	static XPathFactory factory = XPathFactory.newInstance();
    static XPath xpath = factory.newXPath();
    static int threshold;

    public static void main(String[] args) throws TransformerException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, ParseException {

    	if ( args.length < 3 ) {
    		
    		System.err.println("Usage: Soldiers <input-filename> <output-filename> <threshold>");
    		System.exit(1);
    	}
    	
    	String inputfile  = args[0];
    	String outputfile = args[1];
    	threshold = Integer.parseInt(args[2]);
    	
		System.out.printf("input file:  %s\n", inputfile);
		System.out.printf("output file: %s\n\n", outputfile);
		System.out.printf("Search threshold: %d\n\n", threshold);

		Document doc = readDocument(new FileInputStream(inputfile));			 
		doc.normalize();
		
		Connection connection = ConnectionManager.getConnection();
		
		identifyPersonMentionsInPlaceXML(doc, connection);
		
		TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new FileOutputStream(outputfile));	
		transformer.transform(source, result);	
	}
    
	public static Document readDocument(InputStream input) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		
		TransformerFactory tf = TransformerFactory.newInstance();

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream xsl = loader.getResourceAsStream("soldiers/utilities/normal.xsl");

		Transformer transformer = tf.newTransformer(new StreamSource(xsl));
        StreamSource xml = new StreamSource(input);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		transformer.transform(xml, new DOMResult(doc));
		return doc;
	}

	public static void writeDocument(Document doc, OutputStream output) throws TransformerException {
		
		TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        //transformer.setOutputProperty(OutputKeys.METHOD, "xhtml");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(output);	
		//transformer.setOutputProperty(OutputKeys.ENCODING, "windows-1252");
		transformer.transform(source, result);	
	}

	public static void identifyPersonMentionsInPlaceXML(Document doc, Connection connection) throws XPathExpressionException, TransformerConfigurationException, FileNotFoundException, SAXException, ParseException {
		
		Metaphone encoder = new Metaphone();
		Map<String, List<String>> similarNameMap = MakeEncoderMap.getEncoderMap(encoder, connection);

		PersonFinder finder = new PersonFinder();
		finder.enableSimilarNameMatching(encoder, similarNameMap);
		
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
		
		xpath.setNamespaceContext(namespaceContext);
		XPathExpression expr = xpath.compile(".//soldiers:person");
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		int total = list.getLength();
		System.out.println("Number of people to search for: " + total + "\n");
    	
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Element e = (Element) list.item(i);
			Person person = parsePerson(e);
			
			// remove any results from a previous run
			XPathExpression prevcandidate = xpath.compile(".//soldiers:candidate");
			NodeList prevlist = (NodeList) prevcandidate.evaluate(e, XPathConstants.NODESET);
			
			for ( int c = 0; c < prevlist.getLength(); c++ ) {
				
				e.removeChild(prevlist.item(c));				
			}
			
			System.out.printf("%d/%d: %s\n", i+1, total, person.getContent());
			List<Candidate> candidates = finder.findMatches(person, connection);
			
			int bestScore = Integer.MAX_VALUE;
			Iterator<Candidate> iter = candidates.iterator();
			
			Map<Long, Candidate> uniqueCandidates = new HashMap<Long, Candidate>();

			while ( iter.hasNext() ) {
			
				Candidate c = iter.next();
				
				if ( c.getScore().getOverallScore() <= bestScore && c.getScore().getOverallScore() <= threshold ) {

					uniqueCandidates.put(c.getPerson().getSoldierId(), c);
					bestScore = c.getScore().getOverallScore();
				}
				else break;
			}

			for ( Long sid: uniqueCandidates.keySet() ) {
				
				Candidate c = uniqueCandidates.get(sid);
				Person p = c.getPerson();
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
