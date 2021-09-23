package soldiers.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Transformer;
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
import soldiers.database.SoldiersNamespaceContext;
import soldiers.utilities.Soldiers;
import soldiers.utilities.XmlUtils;

public class Filter {

	public static void main(String[] args) throws XPathExpressionException, IllegalArgumentException, FileNotFoundException, SAXException, ParseException, TransformerException {

		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    XmlUtils xmlutils = new XmlUtils();
		Document doc = xmlutils.parse(new File("output/out.xml"));
				
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();

        xpath.setNamespaceContext(namespaceContext);
		XPathExpression expr = xpath.compile("//soldiers:person");
		
		Set<Person> identified = new HashSet<Person>();
		Set<Person> unidentified = new HashSet<Person>();
		Set<Person> ambiguous = new HashSet<Person>();
		
		Map<String, String> setmap = new HashMap<String, String>();
		
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Element e = (Element) list.item(i);
	        Person p = Soldiers.parsePerson(e);
	        
	        NodeList clist = e.getElementsByTagNameNS(namespaceContext.getNamespaceURI("soldiers"), "candidate");
	        
	        if ( clist.getLength() == 1 ) {
	        	
	        	Element candidate = (Element) clist.item(0);
	        	p.setSoldierId(Long.valueOf(candidate.getAttribute("sid")));

	        	identified.add(p);
	        }
	        else if ( clist.getLength() == 1 ) {
	        	
	        	unidentified.add(p);
	        }
	        else {
	        	ambiguous.add(p);
	        	System.out.println(clist.getLength() + " x== "  + p.getContent());
	        	Set<String> names = new HashSet<>();
	        	
	        	for ( int j = 0; j < clist.getLength(); j++ ) {
	        		
	        		Element c = (Element) clist.item(j);

	        		String key = String.format("%s:%s", c.getAttribute("number"), c.getAttribute("sort"));
	        		names.add(key);
		        	
	        		System.out.println(c.getAttribute("content") + " = " + p.getContent() + "? " + c.getAttribute("content").equals(p.getContent()));
	        	}
	        	
	        	System.out.println("-------------- " + names.size());
	        }
		}

		TransformerFactory treansformerFactory = TransformerFactory.newInstance();
		Transformer transformer = treansformerFactory.newTransformer();
		StreamResult result = new StreamResult(new FileOutputStream("output/fixed.xml"));
		
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		
		System.out.println(setmap);
		System.out.println("IDENTIFIED: " + identified.size());
		System.out.println("UNIDENTIFIED: " + unidentified.size());
		System.out.println("AMBIGUOUS: " + ambiguous.size());
		
//		identify(xpath, colln, setmap);
	}
		
}
