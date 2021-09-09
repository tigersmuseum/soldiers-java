package soldiers.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;
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
import soldiers.utilities.Parser;
import soldiers.utilities.Soldiers;
import soldiers.utilities.XmlUtils;

public class Parse {

	public static void main(String[] args) throws XPathExpressionException, TransformerConfigurationException, IllegalArgumentException, FileNotFoundException, SAXException {

		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    XmlUtils xmlutils = new XmlUtils();
		Document doc = xmlutils.parse(new File("/C:/workspaces/development/Tigers/data/collection.xml"));
		
		XPathExpression expr = xpath.compile("//set");
		NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		
		System.out.println(list.getLength());
		
		Set<Person> collection = new HashSet<Person>();
    	
		for ( int i = 0; i < list.getLength(); i++ ) {
		
			Element e = (Element) list.item(i);
			//String text = e.getAttribute("person");
			String text = e.getElementsByTagName("person").item(0).getTextContent();
			
			//text = text.toUpperCase();
			text = text.replaceAll("\\p{javaSpaceChar}", " ").trim();
			text = text.replaceAll("(?i)medal(s)? of", "");
			text = text.replaceAll("(?i)^gift", "");
			text = text.replaceAll("(?i)^.+?the late", "");
			text = text.replaceAll("(?i)^.+?belonging to", "");
			text = text.replaceAll("(?i)^.+?those of", "");
			text = text.replaceAll("(?i)(served).*", "");
			text = text.replaceAll("(?i)(\\d+(ST|ND|RD|TH)?\\s*)(HAMP|HANT|37TH|67TH|BATT|BN|VOL).*", "");
			text = text.replaceAll("(?i)(HAMP|HANT|37TH|67TH|VOL).*", "");
			text = text.replaceAll("(?i)K\\.I\\.A.*", "");
			text = text.trim();
			System.out.println(" ... " + text);
			
			Person p = Parser.parsePersonMention(text);
			System.out.println(text);
			System.out.println(p);
			System.out.println("** " + p.getContent());
			collection.add(p);
		}
		
		Soldiers.writeXml(collection);
	}

}
