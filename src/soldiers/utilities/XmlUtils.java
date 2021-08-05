package soldiers.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

}
