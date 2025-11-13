package soldiers.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
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

public class Inspect {

	static XPathFactory factory = XPathFactory.newInstance();
    static XPath xpath = factory.newXPath();

	public static void main(String[] args) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException, ParseException {

    	if ( args.length < 1 ) {
    		
    		System.err.println("Usage: Inspect <input-filename>");
    		System.exit(1);
    	}
    	
    	String inputfile = args[0];
    	
		Document doc = Soldiers.readDocument(new FileInputStream(inputfile));			 
		doc.normalize();
    	
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
		
		xpath.setNamespaceContext(namespaceContext);
		XPathExpression expr = xpath.compile(".//soldiers:person");
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		int total = list.getLength();
		System.out.println("Number of people in input: " + total + "\n");
		
		Set<Person> people = new HashSet<Person>();
		
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Element e = (Element) list.item(i);
			Person person = Soldiers.parsePerson(e);
			people.add(person);
		}
		
		Map<String, Person> contentMap = new HashMap<String, Person>();
		Map<String, Set<String>> numberMap = new HashMap<String, Set<String>>();
		
		for ( Person person: people ) {
			
			String content = person.getContent();
			contentMap.put(content, person);
			
			Set<Service> record = person.getService();
			
			for ( Service service: record ) {
				
				String number = service.getNumber();
				
				if ( number.length() > 0 ) {
					
					Set<String> values = numberMap.get(number);
					if ( values == null )  values = new HashSet<String>();
					values.add(content);
					numberMap.put(number, values);
				}
			}
		}
		
		Set<Set<String>> sameNumber = new HashSet<Set<String>>();
			
		for ( String number: numberMap.keySet() ) {
			
			Set<String> values = numberMap.get(number);
			
			if ( values.size() > 1 ) {
				
				sameNumber.add(values);
			}
		}

		
		// report
		
		for ( Set<String> values: sameNumber ) {
			 
			System.out.println("More than one name with same number: " + values);
			findDifference(contentMap, values);

		}
	}
	
	public static void findDifference(Map<String, Person> contentMap, Set<String> contents ) {
		
		Set<String> numbers = new HashSet<String>();
		Set<String> surnames = new HashSet<String>();
		Set<String> initials = new HashSet<String>();
		Set<String> ranks = new HashSet<String>();
		
		for ( String content: contents ) {
			
			Person person = contentMap.get(content);
			surnames.add(person.getSurname());
			initials.add(person.getInitials());
			
			Set<Service> record = person.getService();
			
			for ( Service service: record ) {
				
				numbers.add(service.getNumber());
				ranks.add(service.getRank());
			}
		}
		
		
		if ( ranks.size() > 1 && numbers.size() == 1 ) {
			
			System.out.println(ranks);
		}	
		
		if ( surnames.size() > 1 ) {
			
			System.out.println(surnames);
		}
		
		if ( initials.size() > 1 ) {
			
			System.out.println(initials);
		}
	}

}
