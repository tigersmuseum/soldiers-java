package soldiers.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soldiers.database.Person;
import soldiers.database.SoldiersModel;
import soldiers.database.SoldiersNamespaceContext;

public class Bio {

	public static void main(String[] args) throws FileNotFoundException, SAXException, TransformerException, XPathExpressionException {

		if ( args.length == 0 ) {
			
			System.err.println("Usage: Bio <SID>");
			System.exit(-1);
			
		}
		String sid = args[0];
		
		 Connection connection = ConnectionManager.getConnection();
		 Person person = SoldiersModel.getPerson(connection, Long.valueOf(sid));
		 
		 if ( person.getSoldierId() < 0) {
			 
			System.out.println("No record of a soldier with ID: " + sid);
			System.exit(0);
		 }
		 
		 System.out.println(person);
		 
		 getNotes(person);
		 
		 savePersonToFile(person);
		 
		 // name and service details from database
		 
		 // Find (merge?) any biographical notes - find where?
		 
		 // transform results to some report format
	}
	
	public static void savePersonToFile(Person person) throws SAXException, FileNotFoundException, TransformerException {
		
    	XmlUtils xmlutils = new XmlUtils();
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
    	
		Document results = xmlutils.newDocument();
        serializer = tf.newTransformerHandler();
        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setResult(new DOMResult(results));

        serializer.startDocument();
        person.serializePerson(serializer);
        serializer.endDocument();
		
        Soldiers.writeDocument(results, new FileOutputStream("output/bio/" + person.getSoldierId() + ".xml"));
	}

	public static void getNotes(Person person) throws XPathExpressionException, FileNotFoundException, SAXException, TransformerException {
		
		List<File> files = new ArrayList<File>();
		files.add(new File("output/out.xml"));
		
		Map<Long, Set<Note>> notesMap = Collect.makeNoteMapCandidates(files);
		
		Map<Long, Set<Note>> wanted = new HashMap<Long, Set<Note>>();
		wanted.put(person.getSoldierId(), notesMap.get(person.getSoldierId()));
		
		System.out.println(notesMap.get(person.getSoldierId()).size());
		
		Collect.writeXmlFiles(wanted);
	}
}
