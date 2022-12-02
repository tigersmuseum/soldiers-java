package soldiers.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
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
import org.xml.sax.helpers.AttributesImpl;

import soldiers.database.Person;
import soldiers.database.SoldiersModel;
import soldiers.database.SoldiersNamespaceContext;

public class CompareNames {
	
	public static void main(String[] args) throws XPathExpressionException, SAXException, FileNotFoundException, TransformerException, ParseException {

		// Group references to the same soldier from different sources.
		
		// Records for each source are in a separate file (Soldiers XML format). Supply either a folder of such files, or a "worklist"
		// file as a parameter.
		
		if ( args.length < 1 ) {
    		
    		System.err.println("Usage: Collect <file-or-folder>");
    		System.exit(1);
    	}
    	
    	String inputname = args[0];
    	System.out.println(inputname);
    	
    	File inputfile = new File(inputname);   	
    	List<File> work = null;
    	
    	// get a list of Soldiers XML files
    	
    	if ( inputfile.isDirectory() ) {
    		
    		work = Collect.getWorkFromFolder(inputfile);
    	}
    	else {
    		 	
    		work = Collect.getWorkFromFile(inputfile);
    	}
    	
		Set<Long> filter = new HashSet<Long>();
		filter.add((long) 104022); filter.add((long) 131073);

		// Collect sets of note elements - drawn from the various sources, keyed by soldier ID
		// We can either use the sid attribute on the person element for this, or rely on there being a single candidate
		// element - automate this choice?
		
		// here it needs to be candidates .....
		
		// If the filter parameter is null, then all records are collected. Otherwise only soldier ID's in the filter
		// set are collected.
		
	//	Map<Long, Set<Note>> notesMap = makeNoteMapCandidates(work);
		Map<Long, List<Person>> personMap = CompareNames.makeNoteMapIdentified(work, filter);
	//	Map<Long, Set<Note>> notesMap = makeNoteMapIdentified(work, null);

		// output a subset of this info
		
		long[] wanted = {191001, 119075, 104022, 201217, 118369, 170215, 177516, 161705, 118143};
		
		Map<Long, List<Person>> sample = new HashMap<Long, List<Person>>();
		
		for (Long w: wanted) {
			
			sample.put(w, personMap.get(w));
		}
		
		//writeXmlFiles(sample);
		writeCollectedXmlFile(personMap, new File("collected2.xml"));
	}

	
	private static void addToMap(Map<Long, List<Person>> map, Long sid, Person person) {
		
		List<Person> mentions = map.get(sid);
		
		if ( mentions == null ) {
			
			mentions = new ArrayList<Person>();
		}
			
		mentions.add(person);
		map.put(sid, mentions);
	}
	
	
	public static Map<Long, List<Person>> makeNoteMapIdentified(List<File> work, Set<Long> filter) throws XPathExpressionException, ParseException {
		
		// This gets notes for soldiers with a sid attribute set on the person element.
		
    	XmlUtils xmlutils = new XmlUtils();
		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    xpath.setNamespaceContext(new SoldiersNamespaceContext());
		XPathExpression peopleXpr = xpath.compile(".//soldiers:person");
		
		Map<Long, List<Person>> personMap = new HashMap<Long, List<Person>>();
		
    	for ( File file: work ) {
    		
    		System.out.println(file.getPath());
    		
    		Document doc = xmlutils.parse(file);
    		NodeList pList = (NodeList) peopleXpr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
    		
    		for ( int i = 0; i < pList.getLength(); i++ ) {
    			
    			Element person = (Element) pList.item(i);
    			String sidAttr = person.getAttribute("sid");
    			
    			if ( sidAttr.length() > 0 ) {
    				 				
        			Long sid = Long.parseLong(sidAttr);
        			
        			if ( filter == null || filter.contains(sid) ) {
        				
        				Person p = Soldiers.parsePerson(person);
        				   
            			addToMap(personMap, sid, p);
        			}
    			}   			
    		}
    	}

    	return personMap;
	}
	
	
	public static void writeCollectedXmlFile(Map<Long, List<Person>> personMap, File xmlfile) throws SAXException, FileNotFoundException, TransformerException {
		
    	XmlUtils xmlutils = new XmlUtils();
    	
		Document collected = xmlutils.newDocument();
		Element root  = (Element) collected.appendChild(collected.createElement("collected"));

        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
    	
    	System.out.println("size: " + personMap.keySet().size());
    	
    	for ( Long sid: personMap.keySet() ) {
    		
    		List<Person> mentions = personMap.get(sid);

    		if ( mentions.size() >= 0 ) {
    			
    			Person p = SoldiersModel.getPerson(ConnectionManager.getConnection(), sid);
    			
    			Document results = xmlutils.newDocument();
    	        serializer = tf.newTransformerHandler();
    	        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    	        serializer.setResult(new DOMResult(results));

    	        serializer.startDocument();
    	        serializer.startElement(SoldiersModel.XML_NAMESPACE, "mentions", "mentions", new AttributesImpl());
    	        p.serializePerson(serializer);
    	        
    	        for ( Person person: mentions ) {
    	        	
    	        	System.out.println(person);
    	        	person.serializePerson(serializer);
    	        }
   	        
       	        serializer.endElement(SoldiersModel.XML_NAMESPACE, "mentions", "mentions");
    	        serializer.endDocument();
    	        
    	        Element newp = (Element) results.getDocumentElement();
    	        
    	        root.appendChild(collected.importNode(newp, true));
    		}
    	}
    	
        Soldiers.writeDocument(collected, new FileOutputStream(xmlfile));
	}
	
	

}
