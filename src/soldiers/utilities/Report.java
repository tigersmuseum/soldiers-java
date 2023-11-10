package soldiers.utilities;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import soldiers.database.Person;
import soldiers.database.SoldiersModel;

public class Report {
	
	public static void main(String[] args) throws TransformerConfigurationException, SAXException, IllegalArgumentException, FileNotFoundException  {

    	if ( args.length < 2 ) {
    		
    		System.err.println("Usage: Report <soldier-id> <output-filename>");
    		System.exit(1);
    	}

		long sid = Long.valueOf(args[0]);
		String outputfile = args[1];
		
		Person p = SoldiersModel.getPerson(ConnectionManager.getConnection(), sid);
		
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
        serializer = tf.newTransformerHandler();
        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setResult(new StreamResult(new FileOutputStream(outputfile)));

		serializer.startDocument();
		p.serializePerson(serializer);
		serializer.endDocument();
	}

	public static Document getPersonDOM (long sid) throws TransformerConfigurationException, SAXException {
		
		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
		TransformerHandler serializer;

		Person p = SoldiersModel.getPerson(ConnectionManager.getConnection(), sid);

		XmlUtils xmlutils = new XmlUtils();
		Document results = xmlutils.newDocument();
		serializer = tf.newTransformerHandler();
		serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		serializer.setResult(new DOMResult(results));

		serializer.startDocument();
		p.serializePerson(serializer);
		serializer.endDocument();
		
		return results;
	}
}
