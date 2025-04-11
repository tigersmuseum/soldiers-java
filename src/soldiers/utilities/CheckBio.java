package soldiers.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.xml.sax.SAXException;

import soldiers.database.Person;

public class CheckBio {

	public static void main(String[] args) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException, ParseException {

    	if ( args.length < 1 ) {
    		
    		System.err.println("Usage: CheckBio <input-filename>");
    		System.exit(1);
    	}

    	String inputfile  = args[0];
    	
    	XmlUtils xmlutils = new XmlUtils();
   	
		Document doc = xmlutils.readDocument(new FileInputStream(inputfile));
		
		XPath xpath = xmlutils.newXPath();
		XPathExpression bioExpr = xpath.compile("//bio");
		XPathExpression dbPersonExpr = xpath.compile("./database/soldiers:person[1]");
		XPathExpression personExpr = xpath.compile("./soldiers:person[1]");
		XPathExpression sourceExpr = xpath.compile(".//source");
		
		NodeList bioList = (NodeList) bioExpr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		//for ( int i = 0; i < bioList.getLength(); i++ ) {
		for ( int i = 0; i < 1; i++ ) {
			
			Element bioElem = (Element) bioList.item(i);		
			Element dbPersonElem = (Element) dbPersonExpr.evaluate(bioElem, XPathConstants.NODE);
			Person master = Soldiers.parsePerson(dbPersonElem);

			NodeList sourceList = (NodeList) sourceExpr.evaluate(bioElem, XPathConstants.NODESET);
			System.out.println("sources: " + sourceList.getLength());
			
			for ( int j = 0; j < sourceList.getLength(); j++ ) {
				
				Element sourceElem  = (Element) sourceList.item(j);
				String source = sourceElem.getAttribute("name");
				System.out.println(source);
				Element personElem = (Element) personExpr.evaluate(sourceElem, XPathConstants.NODE);
				
				Person p = Soldiers.parsePerson(personElem);
				System.out.println(p);
				
				Compare compare = new Compare(master, p);
				System.out.println(compare);
				compare.makeComparison();

			}
		}
		
	}
	
}
