package soldiers.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.HashMap;
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

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.database.SoldiersNamespaceContext;
import soldiers.utilities.Soldiers;
import soldiers.utilities.XmlUtils;

public class Normalize {

	public static void main(String[] args) throws XPathExpressionException, IllegalArgumentException, FileNotFoundException, SAXException, ParseException, TransformerException {

		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    XmlUtils xmlutils = new XmlUtils();
		Document doc = xmlutils.parse(new File("/C:/workspaces/development/Tigers/data/secondchinawar.xml"));
		
		
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
	//	Element collection = (Element) results.appendChild(results.createElementNS(namespaceContext.getNamespaceURI("soldiers"), "list"));
		
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;

        xpath.setNamespaceContext(namespaceContext);
		XPathExpression expr = xpath.compile(".//soldiers:person");
		XPathExpression notesexpr = xpath.compile(".//soldiers:note");
		
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);

		Map<String, String> ranks = getRanks();
		
		Document collectedResults = xmlutils.newDocument();
		Element collection = (Element) collectedResults.appendChild(collectedResults.createElementNS(namespaceContext.getNamespaceURI("soldiers"), "list"));
		
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Element e = (Element) list.item(i);
	        Person p = Soldiers.parsePerson(e);
	        normalizeRank(p, ranks);

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
		StreamResult result = new StreamResult(new FileOutputStream("output/fixed.xml"));
		
		DOMSource source = new DOMSource(collectedResults);
		transformer.transform(source, result);      
	}
	
	
	public static Map<String, String> getRanks() {
		
		Map<String, String> ranks = new HashMap<String, String>();
		
		ranks.put("hosapp", "Hos App");
		ranks.put("hospitalapprentice", "Hos App");
		ranks.put("pte", "Pte");
		ranks.put("private", "Pte");
		ranks.put("dmr", "Dmr");
		ranks.put("drummer", "Dmr");
		ranks.put("cpl", "Cpl");
		ranks.put("corporal", "Cpl");
		ranks.put("corp", "Cpl");
		ranks.put("sgt", "Sgt");
		ranks.put("serjeant", "Sgt");
		ranks.put("sergeant", "Sgt");
		ranks.put("sergent", "Sgt");
		ranks.put("armsgt", "Sgt");
		ranks.put("paysgt", "Sgt");
		ranks.put("hospsgt", "Sgt");
		ranks.put("orsgt", "Sgt");
		ranks.put("musksgt", "Sgt");
		ranks.put("csgt", "CSgt");
		ranks.put("coloursergeant", "CSgt");
		ranks.put("sgtmaj", "Sgt Maj");
		ranks.put("drummaj", "Drum Maj");
		ranks.put("ens", "Ens");
		ranks.put("ensign", "Ens");
		ranks.put("lt", "Lt");
		ranks.put("asstsurg", "Asst Surg");
		ranks.put("astsurg", "Asst Surg");
		ranks.put("capt", "Capt");
		ranks.put("captain", "Capt");
		ranks.put("adjt", "Adjt");
		ranks.put("adjutant", "Adjt");
		ranks.put("adj", "Adjt");
		ranks.put("qmr", "QM");
		ranks.put("qm", "QM");
		ranks.put("quartermaster", "QM");
		ranks.put("maj", "Maj");
		ranks.put("major", "Maj");
		ranks.put("surg", "Surg");
		ranks.put("surgeon", "Surg");
		ranks.put("ltcol", "Lt Col");
		
		return ranks;
	}
	
	
	private static void normalizeRank(Service service, Map<String, String> ranks) {
		
		if (service.getRank() == null) {
			
			System.out.println("NO RANK: " + service);
			return;
			
		}
		
		String raw = service.getRank().toLowerCase().trim();
		raw = raw.replaceAll("/", "");
		raw = raw.replaceAll("\\.", "");
		raw = raw.replaceAll("\\s+", "");
		String normal = ranks.get(raw);
		
		if ( normal != null ) {
			
			service.setRank(normal);
		}
		else {
			
			System.out.println("no rank for: " + raw);
		}
		
	}
	

	private static void normalizeRank(Person person, Map<String, String> ranks) {
		
		//System.out.println(person.getSurname());
		for ( Service service: person.getService() ) {
			
			normalizeRank(service, ranks);			
		}
		
	}

}
