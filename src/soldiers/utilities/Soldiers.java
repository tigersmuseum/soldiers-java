package soldiers.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.database.SoldiersModel;
import soldiers.database.SoldiersNamespaceContext;

public class Soldiers {

	static XPathFactory factory = XPathFactory.newInstance();
    static XPath xpath = factory.newXPath();

    public static void main(String[] args) throws TransformerException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {

    	casldgr();
	}
        
    public static void casldgr() throws TransformerException, XPathExpressionException, SAXException, ParserConfigurationException, IOException {
    	
    	String filenameIn = "/H:/Archive/Admin/Database/WOCL/wocl.xml";

		Document doc = readDocument(new FileInputStream(filenameIn));			 
		doc.normalizeDocument();
		
		identifyPersonMentionsInPlaceXML(doc);
		
		TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new FileOutputStream("output/out.xml"));	
		transformer.transform(source, result);	
    }

    
    public static void addTigerID(Document doc) throws FileNotFoundException, TransformerException, XPathExpressionException {
    	
    	//Connection connection = DerbyConnect.getDerbyConnection();
    	Connection connection = DerbyConnect.getConnection();
    	
		XPathExpression people = xpath.compile("//person");			
		XPathExpression index = xpath.compile("./index");	
		
		NodeList list = (NodeList) people.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
    	
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Element e = (Element) list.item(i);
			int sid = Integer.valueOf((String) index.evaluate(e, XPathConstants.STRING));
			long tid = SoldiersModel.getTigerIdForSourceItem(connection, "CHARLIE", sid);
			
			if ( tid > 0 ) e.setAttribute("tid", String.valueOf(tid));
		}
		
		writeDocument(doc, new FileOutputStream("delete.xml"));
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

	public static void identifyPersonMentionsInPlaceXML(Document doc) throws XPathExpressionException, TransformerConfigurationException, FileNotFoundException, SAXException {
		
		xpath.setNamespaceContext(new SoldiersNamespaceContext());
		XPathExpression expr = xpath.compile(".//soldiers:person");
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
    	
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Person person = new Person();

			Element e = (Element) list.item(i);
			
			String surname = e.getElementsByTagName("surname").getLength() == 0 ? null : e.getElementsByTagName("surname").item(0).getTextContent();
			String forenames = e.getElementsByTagName("forenames").getLength() == 0 ? null : e.getElementsByTagName("forenames").item(0).getTextContent();
			String initials = e.getElementsByTagName("initials").getLength() == 0 ? null : e.getElementsByTagName("initials").item(0).getTextContent();
			String suffix = e.getElementsByTagName("suffix").getLength() == 0 ? null : e.getElementsByTagName("suffix").item(0).getTextContent();
						
			NodeList serviceList = e.getElementsByTagName("service");

			for ( int s = 0; s < serviceList.getLength(); s++ ) {
				
				Element svc = (Element) serviceList.item(s);
				
				Service ss = new Service();
				ss.setNumber(svc.getAttribute("number"));
				ss.setRank(svc.getAttribute("rank"));
				ss.setRegiment(svc.getAttribute("regiment"));
				ss.setUnit(svc.getAttribute("unit"));
				person.addService(ss);
			}
			
			person.setSurname(surname);
			person.setInitials(initials);
			person.setForenames(forenames);
			person.setSuffix(suffix);
			
			System.out.println(person.getContent());
			Set<Person> candidates = SearchSoldier.checkIdentity(person);
			
			AttributesImpl attr= new AttributesImpl();
			attr.addAttribute("", "hits",  "hits", "Integer", String.valueOf(candidates.size()));
			
			for (Person p: candidates) {
			
				Set<Service> service = p.getService();
				Service svc = service.iterator().next();

				//p.serializePerson(ch);
				Element candidate = doc.createElement("candidate");
				candidate.setAttribute("tid", String.format("%d", p.getSoldierId()));
				candidate.setAttribute("content", p.getContent());
				candidate.setAttribute("sort", p.getSort());
				candidate.setAttribute("number", svc.getNumber());
				candidate.setAttribute("rank", svc.getRank());
				e.appendChild(candidate);
				System.out.println("=" + p.getContent());
			}
			
			System.out.println("----------------");
		}
		
	}

	public static Person parsePerson(Element element) {
		
		Person person = new Person();
		
		String tidattr = element.getAttribute("tid");
		if ( tidattr != null && tidattr.length() > 0 ) person.setSoldierId(Long.valueOf(tidattr));
		
		String surname = element.getElementsByTagName("surname").getLength() == 0 ? null : element.getElementsByTagName("surname").item(0).getTextContent();
		String forenames = element.getElementsByTagName("forenames").getLength() == 0 ? null : element.getElementsByTagName("forenames").item(0).getTextContent();
		String initials = element.getElementsByTagName("initials").getLength() == 0 ? null : element.getElementsByTagName("initials").item(0).getTextContent();
		String suffix = element.getElementsByTagName("suffix").getLength() == 0 ? null : element.getElementsByTagName("suffix").item(0).getTextContent();

		String number = null;
		String rank = element.getElementsByTagName("rank").getLength() == 0 ? null : element.getElementsByTagName("rank").item(0).getTextContent();
		
		Element service = (Element) element.getElementsByTagName("service").item(0);
		
		if ( service != null ) {
			
			number = service.getAttribute("number");
			String rankattr = service.getAttribute("rank");
			if ( rankattr != null && rankattr.length() > 0 )  rank = rankattr; 
		}
		
		Service svc = new Service();

		svc.setNumber(number);
		svc.setRank(rank);
		person.setSurname(surname);
		person.setInitials(initials);
		person.setForenames(forenames);
		person.setSuffix(suffix);
		person.addService(svc);
		
		return person;
	}
	
	public static void writeXml(Collection<Person> people) throws TransformerConfigurationException, SAXException, IllegalArgumentException, FileNotFoundException {
		

        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
        serializer = tf.newTransformerHandler();
        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setResult(new StreamResult(new FileOutputStream("output/temp.xml")));

		serializer.startDocument();
		serializer.startElement("", "data", "data", new AttributesImpl());
		
		for (Person person : people ) {
			
			person.serializePerson(serializer);
		}	
		
		serializer.endElement("", "data", "data");
		serializer.endDocument();
	}

}
