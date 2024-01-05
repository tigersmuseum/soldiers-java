package soldiers.utilities;

import java.io.File;
import java.sql.Connection;
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

    	if ( args.length < 1 ) {
    		
    		System.err.println("Usage: Insert <filename>");
    		System.exit(1);
    	}
    	
    	String inputfile = args[0];
    	Connection connection = ConnectionManager.getConnection();
    	long nextId = SoldiersModel.getNextAvailableSoldierId(connection);

    	XmlUtils xmlutils = new XmlUtils();
		Document doc = xmlutils.parse(new File(inputfile));
		
		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    xpath.setNamespaceContext(new SoldiersNamespaceContext());
		XPathExpression expr = xpath.compile(".//soldiers:person");
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Person person = Soldiers.parsePerson((Element) list.item(i));
			if ( person.getSoldierId() < 0 )  person.setSoldierId(nextId++);
			
			System.out.println(person);
			SoldiersModel.insertPerson(ConnectionManager.getConnection(), person);
			System.out.println("-----------");
		}
		
		// If you run this twice with the same input file then you'll end up with the same data added to the database twice.
		// To help try and avoid this, we delete the input file once its been added to the database.
		File file = new File(inputfile);
		file.delete();
	}

}
