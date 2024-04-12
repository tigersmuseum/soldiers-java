package soldiers.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
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
import soldiers.database.SoldiersNamespaceContext;
import soldiers.text.Parser;

public class ParseSpreadsheet {

	public static void main(String[] args) throws XPathExpressionException, SAXException, TransformerException, MalformedURLException, ParseException, FileNotFoundException {

	    XmlUtils xmlutils = new XmlUtils();
		
		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
		xpath.setNamespaceContext(namespaceContext);
		
		Document input = xmlutils.parse(new File("/J:/Archive/Admin/Database/eclipse-workspace/Tigers/output/spreadsheet.xml"));
		input.normalize();
		
		XPathExpression personExpr = xpath.compile("//cell[@col = 'Awarded_to']");
		NodeList list = (NodeList) personExpr.evaluate(input.getDocumentElement(), XPathConstants.NODESET);
		
		System.out.println(list.getLength());
		
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
        serializer = tf.newTransformerHandler();
        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setResult(new StreamResult(new FileOutputStream("output/temp.xml")));

		serializer.startDocument();
		serializer.startElement("", "data", "data", new AttributesImpl());

		for ( int i = 0; i < list.getLength(); i++ ) {

			Element e = (Element) list.item(i);
			System.out.println(e.getTextContent());
			String txt = e.getTextContent().split("Hamp")[0].split("Hant")[0];
			
			List<Person> plist = Parser.findMention(txt);
			
			for ( Person person: plist ) {
				
				System.out.println(person + " -- " + person.getService().iterator().next());
				person.serializePerson(serializer);		
			}
		}		
		
		serializer.endElement("", "data", "data");
		serializer.endDocument();
	}
}
