package soldiers.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
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
import soldiers.database.SoldiersModel;
import soldiers.database.SoldiersNamespaceContext;

public class Collect {

	public static void main(String[] args) throws XPathExpressionException, SAXException, FileNotFoundException, TransformerException {

    	if ( args.length < 1 ) {
    		
    		System.err.println("Usage: Collect <folder>");
    		System.exit(1);
    	}
    	
    	String foldername = args[0];
    	
    	File folder = new File(foldername);
    	
    	if ( ! folder.isDirectory() ) {
    	
    		System.err.println(foldername + " is not a directory.");
    		System.exit(-1);
    	}
    	
    	XmlUtils xmlutils = new XmlUtils();
		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    xpath.setNamespaceContext(new SoldiersNamespaceContext());
		XPathExpression peopleXpr = xpath.compile(".//soldiers:person");
		XPathExpression notesXpr = xpath.compile(".//soldiers:note");
		
		Map<Long, List<Element>> notesMap = new HashMap<Long, List<Element>>();

    	for ( File file: folder.listFiles() ) {
    		
    		Document doc = xmlutils.parse(file);
    		NodeList pList = (NodeList) peopleXpr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);

    		for ( int i = 0; i < pList.getLength(); i++ ) {
    		
    			Element person = (Element) pList.item(i);
    			String sidAttr = person.getAttribute("sid");
    			
    			if ( sidAttr.length() > 0 ) {
    				 				
        			Long sid = Long.parseLong(sidAttr);
        			
            		NodeList nList = (NodeList) notesXpr.evaluate(person, XPathConstants.NODESET);
        			
            		for ( int n = 0; n < nList.getLength(); n++ ) {
            			
            			addToMap(notesMap, sid, (Element) nList.item(n));
            		}
    			}   			
    		}
    	}
    	
    	//
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
    	
    	NoteDateComparator comparator = new NoteDateComparator();
    	
    	for ( Long sid: notesMap.keySet() ) {
    		
    		List<Element> notes = notesMap.get(sid);
    		notes.sort(comparator);
    		System.out.println(sid + " = " + notes.size());
    		
    		for ( Element e: notes ) {
    			
    			System.out.println(e.getAttribute("date"));
    		}
    		
    		if ( notes.size() >= 3 ) {
    			
    			Person p = SoldiersModel.getPerson(ConnectionManager.getConnection(), sid);
    			
    			Document results = xmlutils.newDocument();
    	        serializer = tf.newTransformerHandler();
    	        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    	        serializer.setResult(new DOMResult(results));

    	        serializer.startDocument();
    	        p.serializePerson(serializer);
    	        serializer.endDocument();
    	        
    	        Element newp = (Element) results.getDocumentElement();
    	        
    	        for ( Element note: notes ) {
    	        	
    	        	Element n = (Element) results.importNode(note, true);
    	        	newp.appendChild(n);
    	        }
    	        
    	        Soldiers.writeDocument(results, new FileOutputStream("output/xxx/" + sid + ".xml"));
    		}
    	}
	}

	
	public static void addToMap(Map<Long, List<Element>> map, Long sid, Element element) {
		
		List<Element> notes = map.get(sid);
		
		if ( notes == null ) {
			
			notes = new ArrayList<Element>();
		}
		
		notes.add(element);
		map.put(sid, notes);
	}
}
