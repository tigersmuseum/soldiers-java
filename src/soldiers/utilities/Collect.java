package soldiers.utilities;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import soldiers.database.SoldiersNamespaceContext;

public class Collect {

	public static void main(String[] args) throws XPathExpressionException {

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
		
		Map<Long, Set<Element>> notesMap = new HashMap<Long, Set<Element>>();

    	for ( File file: folder.listFiles() ) {
    		
    		Document doc = xmlutils.parse(file);
    		NodeList pList = (NodeList) peopleXpr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);

    		for ( int i = 0; i < pList.getLength(); i++ ) {
    		
    			Element person = (Element) pList.item(i);
    			Long sid = Long.parseLong(person.getAttribute("sid"));
    			
        		NodeList nList = (NodeList) notesXpr.evaluate(person, XPathConstants.NODESET);
    			
        		for ( int n = 0; n < nList.getLength(); n++ ) {
        			
        			addToMap(notesMap, sid, (Element) nList.item(n));
        		}
    		}
    	}
    	
    	//
	}

	
	public static void addToMap(Map<Long, Set<Element>> map, Long sid, Element element) {
		
		Set<Element> notes = map.get(sid);
		
		if ( notes == null ) {
			
			notes = new HashSet<Element>();
		}
		
		notes.add(element);
		map.put(sid, notes);
	}
}
