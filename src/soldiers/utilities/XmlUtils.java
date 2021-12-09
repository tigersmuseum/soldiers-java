package soldiers.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import soldiers.database.Person;

public class XmlUtils {

	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;

	public XmlUtils() {

		factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		factory.setExpandEntityReferences(false);
		
		try {
			builder = factory.newDocumentBuilder();
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	
	public Document newDocument() {
		
		return builder.newDocument();
	}
	
	
	public DocumentBuilder getBuilder() {
		return builder;
	}
	
	public Document parse(String xml) {
		
		Document doc = null;
		try {
			doc = builder.parse(new InputSource(new StringReader(xml)));
		}
		catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doc;
	}
	
	
	public Document parse(File file) {
		
		Document doc = null;
		try {
			doc = builder.parse(new FileInputStream(file));
		}
		catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return doc;
	}

	
	public static void writeXml(Collection<Person> people, FileOutputStream file) throws TransformerConfigurationException, SAXException, IllegalArgumentException {
		
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
        serializer = tf.newTransformerHandler();
        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setResult(new StreamResult(file));

		serializer.startDocument();
		serializer.startElement("", "list", "list", new AttributesImpl());
		
		for (Person person : people ) {
			
			person.serializePerson(serializer);
		}	
		
		serializer.endElement("", "list", "list");
		serializer.endDocument();
	}



	public static void writeDocument(Document doc, OutputStream output) throws TransformerException {
		
		TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
		DOMSource source = new DOMSource(doc);
		System.out.println("DDD " + doc);
		StreamResult result = new StreamResult(output);	
		transformer.setOutputProperty(OutputKeys.ENCODING, "windows-1252");
		transformer.transform(source, result);	
	}

}
