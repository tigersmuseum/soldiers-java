package soldiers.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
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
import org.xml.sax.helpers.AttributesImpl;

import soldiers.database.Person;
import soldiers.database.SoldiersModel;
import soldiers.database.SoldiersNamespaceContext;

public class Collect {
	
	static PrintWriter outwriter;

	public static void main(String[] args) throws XPathExpressionException, SAXException, FileNotFoundException, TransformerException {

		// group references to the same soldier from different sources.
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
    	
    	File outputfile = new File("lonlist.csv");
    	outwriter = new PrintWriter(outputfile);
    	
    	// get a list of Soldiers XML files
    	
    	if ( inputfile.isDirectory() ) {
    		
    		work = getWorkFromFolder(inputfile);
    	}
    	else {
    		 	
    		work = getWorkFromFile(inputfile);
    	}
    	
    	// Collect sets of note elements - drawn from the various sources, keyed by soldier ID
		Map<Long, Set<Note>> notesMap = makeNoteMapCandidates(work);
		//Map<Long, Set<Note>> notesMap = makeNoteMapIdentified(work);

		// output a subset of this info
		
		long[] wanted = {191001, 119075, 104022, 201217, 118369, 170215, 177516, 161705, 118143};
		
		Map<Long, Set<Note>> sample = new HashMap<Long, Set<Note>>();
		
		for (Long w: wanted) {
			
			sample.put(w, notesMap.get(w));
		}
		
		//writeXmlFiles(sample);
		writeCollectedXmlFile(sample, new File("collected.xml"));
		
		outwriter.close();
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
	
	
	public static Map<Long, Set<Note>> makeNoteMapIdentified(List<File> work) throws XPathExpressionException {
		
		// This gets notes for soldiers with a sid attribute set on the person element.
		
    	XmlUtils xmlutils = new XmlUtils();
		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    xpath.setNamespaceContext(new SoldiersNamespaceContext());
		XPathExpression peopleXpr = xpath.compile(".//soldiers:person");
		XPathExpression notesXpr = xpath.compile(".//soldiers:note");
		
		Map<Long, Set<Note>> notesMap = new HashMap<Long, Set<Note>>();
		
    	for ( File file: work ) {
    		
    		System.out.println(file.getPath());
    		
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
            			System.out.printf("%d,%s,%s,%s\n", sid, ((Element) nList.item(n)).getAttribute("source"), ((Element) nList.item(n)).getAttribute("sourceref"), ((Element) nList.item(n)).getAttribute("type"));
            		}
    			}   			
    		}
    	}

    	return notesMap;
	}
	
	
	public static Map<Long, Set<Note>> makeNoteMapCandidates(List<File> work) throws XPathExpressionException {
		
		// This gets notes for soldiers with a sid attribute in a candidate element. If there is more than one
		// candidate element the results will be ambiguous.

		XmlUtils xmlutils = new XmlUtils();
		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    xpath.setNamespaceContext(new SoldiersNamespaceContext());
		XPathExpression peopleXpr = xpath.compile(".//soldiers:person[soldiers:candidate]");
		XPathExpression notesXpr = xpath.compile(".//soldiers:note");
		
		Map<Long, Set<Note>> notesMap = new HashMap<Long, Set<Note>>();
		
    	for ( File file: work ) {
    		
    		Document doc = xmlutils.parse(file);
    		NodeList pList = (NodeList) peopleXpr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);

			System.out.println(pList.getLength());
			int max = 0;
			
    		for ( int i = 0; i < pList.getLength(); i++ ) {
    		
    			Element person = (Element) pList.item(i);
    			
    			NodeList cList = person.getElementsByTagNameNS(SoldiersModel.XML_NAMESPACE, "candidate");
    			
    			if ( cList.getLength() == 1 ) {
    				
        	   		for ( int j = 0; j < cList.getLength(); j++ ) {
            	   		
            			Element candidate = (Element) cList.item(j);
            			String sidAttr = candidate.getAttribute("sid");
            			
            			if ( sidAttr.length() > 0 ) {
            				 				
                			Long sid = Long.parseLong(sidAttr);
                			
                    		NodeList nList = (NodeList) notesXpr.evaluate(person, XPathConstants.NODESET);
                			
                    		for ( int n = 0; n < nList.getLength(); n++ ) {
                    			
                    			addToMap(notesMap, sid, new Note((Element) nList.item(n)));
                    			outwriter.printf("%d,\"%s\",\"%s\",%s\n", sid, ((Element) nList.item(n)).getAttribute("source"), ((Element) nList.item(n)).getAttribute("sourceref"), ((Element) nList.item(n)).getAttribute("type"));
                    		}
                    		
                    		if ( notesMap.get(sid) != null && notesMap.get(sid).size() > max ) {
                    			
                    			System.out.println("max " + max++ + " = " + sid);
                    		}
            			}   			
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
    		if ( notesMap.get(sid) != null ) notes.addAll(notesMap.get(sid));

    		notes.sort(comparator);
    		
    		if ( notes.size() >= 0 ) {
    			
    			Person p = SoldiersModel.getPerson(ConnectionManager.getConnection(), sid);
    			
    			String rec = String.format("%-30s %10d %6d", p.getSort(), p.getSoldierId(), notes.size());
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
    		}
    	}
    	
    	for (String r: records) {
    		
    		System.out.println(r);
    	}
	}
	
	
	public static void writeCollectedXmlFile(Map<Long, Set<Note>> notesMap, File xmlfile) throws SAXException, FileNotFoundException, TransformerException {
		
    	XmlUtils xmlutils = new XmlUtils();
    	
		Document collected = xmlutils.newDocument();
		Element root  = (Element) collected.appendChild(collected.createElement("collected"));

        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
    	
    	NoteDateComparator comparator = new NoteDateComparator();
    	
    	System.out.println("size: " + notesMap.keySet().size());
    	
    	for ( Long sid: notesMap.keySet() ) {
    		
    		List<Note> notes = new ArrayList<Note>();
    		if ( notesMap.get(sid) != null ) notes.addAll(notesMap.get(sid));
    		
    		System.out.println(notes.size());

    		notes.sort(comparator);
    		
    		if ( notes.size() >= 0 ) {
    			
    			Person p = SoldiersModel.getPerson(ConnectionManager.getConnection(), sid);
    			
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
    	        
    	        root.appendChild(collected.importNode(newp, true));
    		}
    	}
    	
        Soldiers.writeDocument(collected, new FileOutputStream(xmlfile));
	}

}
