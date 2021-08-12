package soldiers.test;

import java.io.File;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import soldiers.database.Person;
import soldiers.utilities.Parser;
import soldiers.utilities.XmlUtils;

public class Parse {

	public static void main(String[] args) throws XPathExpressionException {

		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    XmlUtils xmlutils = new XmlUtils();
		Document doc = xmlutils.parse(new File("/C:/workspaces/development/Tigers/output/accessions.xml"));
		
		XPathExpression expr = xpath.compile("//accession[starts-with(@number, 'A')][MEDAL]/history");
		NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		
		System.out.println(list.getLength());
    	
		for ( int i = 0; i < list.getLength(); i++ ) {
		
			String text = list.item(i).getTextContent();
			Person p = Parser.parsePersonMention(text);
			System.out.println(text);
			System.out.println("** " + p.getContent());
		}
	}

}
