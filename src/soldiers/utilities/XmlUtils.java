package soldiers.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Collection;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import soldiers.database.Person;
import soldiers.database.SoldiersNamespaceContext;

public class XmlUtils {

	private DocumentBuilderFactory docFactory;
	private DocumentBuilder builder;
	private XPathFactory pathFactory = XPathFactory.newInstance();

	public XmlUtils() {

		docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setValidating(false);
		docFactory.setNamespaceAware(true);
		docFactory.setExpandEntityReferences(false);
		
		pathFactory = XPathFactory.newInstance();
		
		try {
			builder = docFactory.newDocumentBuilder();
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
		StreamResult result = new StreamResult(output);	
		transformer.setOutputProperty(OutputKeys.ENCODING, "windows-1252");
		transformer.transform(source, result);	
	}

	public XPath newXPath() {
		
		XPath xpath = pathFactory.newXPath();
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
		xpath.setNamespaceContext(namespaceContext);
		return xpath;
	}
	
	public Document readDocument(InputStream input) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		
		TransformerFactory tf = TransformerFactory.newInstance();

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream xsl = loader.getResourceAsStream("soldiers/utilities/normal.xsl");

		Transformer transformer = tf.newTransformer(new StreamSource(xsl));
        StreamSource xml = new StreamSource(input);
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setNamespaceAware(true);
		DocumentBuilder builder = docFactory.newDocumentBuilder();
		Document doc = builder.newDocument();
		transformer.transform(xml, new DOMResult(doc));
		return doc;
	}

	public static ContentHandler getSerializer(FileOutputStream file) {

        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
        try {
			serializer = tf.newTransformerHandler();
	        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	        serializer.setResult(new StreamResult(file));
			return serializer;
		}
        catch (TransformerConfigurationException e) {
			e.printStackTrace();
			return null;
		}

	}
}
