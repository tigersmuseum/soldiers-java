package soldiers.utilities;

import java.io.File;
import java.text.ParseException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import soldiers.database.Person;
import soldiers.database.SoldiersModel;
import soldiers.database.SoldiersNamespaceContext;

public class Insert {

	public static void main(String[] args) throws XPathExpressionException, ParseException {

	    XmlUtils xmlutils = new XmlUtils();
		Document doc = xmlutils.parse(new File("/C:/workspaces/development/Tigers/input/new.xml"));
		
		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    xpath.setNamespaceContext(new SoldiersNamespaceContext());
		XPathExpression expr = xpath.compile(".//soldiers:person");
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Person person = Soldiers.parsePerson((Element) list.item(i));
			
			System.out.println(person);
			SoldiersModel.insertPerson(ConnectionManager.getConnection(), person);
			System.out.println("-----------");
		}
	}

}
