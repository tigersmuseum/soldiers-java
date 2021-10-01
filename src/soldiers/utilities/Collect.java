package soldiers.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
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

import soldiers.database.Person;
import soldiers.database.SoldiersModel;
import soldiers.database.SoldiersNamespaceContext;

public class Collect {

	public static void main(String[] args) throws XPathExpressionException, SAXException, FileNotFoundException, TransformerException {

    	if ( args.length < 1 ) {
    		
    		System.err.println("Usage: Collect <folder>");
    		System.exit(1);
    	}
    	
    	String inputname = args[0];
    	
    	File inputfile = new File(inputname);   	
    	List<File> work = null;
    	
    	if ( inputfile.isDirectory() ) {
    		
    		work = getWorkFromFolder(inputfile);
    	}
    	else {
    		 	
    		work = getWorkFromFile(inputfile);
    	}
    	
		Map<Long, Set<Note>> notesMap = makeNoteMap(work);
		System.out.println(notesMap.size());


		//
		
		long[] wanted = {201200, 201204, 201215};
		
		Map<Long, Set<Note>> sample = new HashMap<Long, Set<Note>>();
		
		for (Long w: wanted) {
			
			sample.put(w, notesMap.get(w));
		}
		
		writeXmlFiles(sample);
	}

	
	public static List<File> getWorkFromFolder(File folder) {
		
		List<File> work = new ArrayList<File>();
    	
    	for ( File file: folder.listFiles() ) {
    		
    		work.add(file);
    	}
    	
    	return work;
	}
	
	
	public static List<File> getWorkFromFile(File file) {
		
		List<File> work = new ArrayList<File>();
		
    	XmlUtils xmlutils = new XmlUtils();
		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();
	    
		try {
			XPathExpression workXpr = xpath.compile(".//source");
    		Document doc = xmlutils.parse(file);
    		NodeList list = (NodeList) workXpr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);

    		for ( int i = 0; i < list.getLength(); i++ ) {
    		
    			Element e = (Element) list.item(i);
    			String filename = e.getAttribute("filename");
    			File sourcefile = new File(filename);
    			work.add(sourcefile);
    		}
		}
		catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	    
	    return work;
	}

	
	public static void addToMap(Map<Long, Set<Note>> map, Long sid, Note note) {
		
		Set<Note> notes = map.get(sid);
		
		if ( notes == null ) {
			
			notes = new HashSet<Note>();
		}
			
		notes.add(note);
		map.put(sid, notes);
	}
	
	
	public static Map<Long, Set<Note>> makeNoteMap(List<File> work) throws XPathExpressionException {
		
    	XmlUtils xmlutils = new XmlUtils();
		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    xpath.setNamespaceContext(new SoldiersNamespaceContext());
		XPathExpression peopleXpr = xpath.compile(".//soldiers:person");
		XPathExpression notesXpr = xpath.compile(".//soldiers:note");
		
		Map<Long, Set<Note>> notesMap = new HashMap<Long, Set<Note>>();
		
    	for ( File file: work ) {
    		
    		Document doc = xmlutils.parse(file);
    		NodeList pList = (NodeList) peopleXpr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);

    		for ( int i = 0; i < pList.getLength(); i++ ) {
    		
    			Element person = (Element) pList.item(i);
    			String sidAttr = person.getAttribute("sid");
    			
    			if ( sidAttr.length() > 0 ) {
    				 				
        			Long sid = Long.parseLong(sidAttr);
        			
            		NodeList nList = (NodeList) notesXpr.evaluate(person, XPathConstants.NODESET);
        			
            		for ( int n = 0; n < nList.getLength(); n++ ) {
            			
            			addToMap(notesMap, sid, new Note((Element) nList.item(n)));
            		}
    			}   			
    		}
    	}

    	return notesMap;
	}
	
	public static void writeXmlFiles(Map<Long, Set<Note>> notesMap) throws SAXException, FileNotFoundException, TransformerException {
		
    	XmlUtils xmlutils = new XmlUtils();
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
    	
    	NoteDateComparator comparator = new NoteDateComparator();
    	
    	List<String> records = new ArrayList<String>();
    	
    	for ( Long sid: notesMap.keySet() ) {
    		
    		List<Note> notes = new ArrayList<Note>();
    		notes.addAll(notesMap.get(sid));
    		notes.sort(comparator);
    		
    		if ( notes.size() >= 0 ) {
    			
    			Person p = SoldiersModel.getPerson(ConnectionManager.getConnection(), sid);
    			
    			String rec = String.format("%-30s %10d", p.getSort(), p.getSoldierId());
    			records.add(rec);
    			
    			Document results = xmlutils.newDocument();
    	        serializer = tf.newTransformerHandler();
    	        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    	        serializer.setResult(new DOMResult(results));

    	        serializer.startDocument();
    	        p.serializePerson(serializer);
    	        serializer.endDocument();
    	        
    	        Element newp = (Element) results.getDocumentElement();
    	        
    	        for ( Note note: notes ) {
    	        	
    	        	Element n = (Element) results.importNode(note.getElement(), true);
    	        	newp.appendChild(n);
    	        }
    	        
    	        Soldiers.writeDocument(results, new FileOutputStream("output/bio/" + sid + ".xml"));
    	    	
    	    	Collections.sort(records);
    	    	
    	    	for (String r: records) {
    	    		
    	    		System.out.println(r);
    	    	}
    		}
    	}
	}
}
