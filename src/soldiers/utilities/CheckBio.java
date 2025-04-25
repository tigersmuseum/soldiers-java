package soldiers.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import soldiers.database.Person;
import soldiers.database.SoldiersModel;

public class CheckBio {

	public static void main(String[] args) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException, ParseException {

    	if ( args.length < 2 ) {
    		
    		System.err.println("Usage: CheckBio <input-filename> <output-filename>");
    		System.exit(1);
    	}

    	String inputfile  = args[0];
       	String outputfile = args[1];
    	
    	XmlUtils xmlutils = new XmlUtils();
   	
		Document doc = xmlutils.readDocument(new FileInputStream(inputfile));
		
		XPath xpath = xmlutils.newXPath();
		XPathExpression bioExpr = xpath.compile("//bio");
		XPathExpression dbPersonExpr = xpath.compile("./database/soldiers:person[1]");
		XPathExpression personExpr = xpath.compile("./soldiers:person[1]");
		XPathExpression sourceExpr = xpath.compile(".//source");
		
		
        ContentHandler serializer = XmlUtils.getSerializer(new FileOutputStream(outputfile));

		serializer.startDocument();
		serializer.startElement(SoldiersModel.XML_NAMESPACE, "list", "list", new AttributesImpl());

		NodeList bioList = (NodeList) bioExpr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int i = 0; i < bioList.getLength(); i++ ) {

			Element bioElem = (Element) bioList.item(i);		
			Element dbPersonElem = (Element) dbPersonExpr.evaluate(bioElem, XPathConstants.NODE);
			Person master = Soldiers.parsePerson(dbPersonElem);
			
			serializer.startElement(SoldiersModel.XML_NAMESPACE, "delta", "delta", new AttributesImpl());
			
			serializer.startElement(SoldiersModel.XML_NAMESPACE, "database", "database", new AttributesImpl());
			master.serializePerson(serializer);
			serializer.endElement(SoldiersModel.XML_NAMESPACE, "database", "database");

			NodeList sourceList = (NodeList) sourceExpr.evaluate(bioElem, XPathConstants.NODESET);
			
			for ( int j = 0; j < sourceList.getLength(); j++ ) {
				
				Element sourceElem  = (Element) sourceList.item(j);
				String source = sourceElem.getAttribute("name");
				
				AttributesImpl srcAttr = new AttributesImpl();
				srcAttr.addAttribute("", "name", "name", "String", source);
				serializer.startElement(SoldiersModel.XML_NAMESPACE, "source", "source", srcAttr);

				System.out.println(j + ": " + source);
				Element personElem = (Element) personExpr.evaluate(sourceElem, XPathConstants.NODE);
				
				Person p = Soldiers.parsePerson(personElem);
				
				Compare compare = new Compare(master, p);
				Person diffs = compare.makeComparison();
				
				diffs.serializePerson(serializer);

				System.out.println("--------------------- ");
				serializer.endElement(SoldiersModel.XML_NAMESPACE, "source", "source");
			}

			serializer.endElement(SoldiersModel.XML_NAMESPACE, "delta", "delta");
			serializer.endDocument();
		}
		
		serializer.endElement(SoldiersModel.XML_NAMESPACE, "list", "list");
		serializer.endDocument();
		
	}
	
}
