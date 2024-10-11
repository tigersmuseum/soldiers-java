package soldiers.test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.database.SoldiersModel;
import soldiers.utilities.ConnectionManager;

public class ScratchQuery {

	static XPathFactory factory = XPathFactory.newInstance();
    static XPath xpath = factory.newXPath();

	public static void main(String[] args) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {

		String sql = "select P1.SID, P2.SID from SERVICE as S1, SERVICE as S2, PERSON as P1, PERSON as P2 where P1.SURNAME = P2.SURNAME and P1.INITIALS = P2.INITIALS and P1.FORENAMES = P2.FORENAMES and P1.DEATH = P2.DEATH and S1.SID = P1.SID and S2.SID = P2.SID and P1.SID < P2.SID";
		
		Connection connection = ConnectionManager.getConnection();
		
		long sid1, sid2;

        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
        serializer = tf.newTransformerHandler();
        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setResult(new StreamResult(new FileOutputStream("output/results.xml")));

		serializer.startDocument();
		serializer.startElement(SoldiersModel.XML_NAMESPACE, "list", "list", new AttributesImpl());

        try {
			
			PreparedStatement stmt = connection.prepareStatement(sql);
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				//serializer.startElement(SoldiersModel.XML_NAMESPACE, "item", "item", new AttributesImpl());
				sid1 = results.getInt(1);
				sid2 = results.getInt(2);
				Person p1 = SoldiersModel.getPerson(connection, sid1);
				Person p2 = SoldiersModel.getPerson(connection, sid2);
				Set<Service> s = p2.getService();
				
				for (Service service: s ) {
					p1.addService(service);
				}
				p1.serializePerson(serializer);
				//p2.serializePerson(serializer);
			   	//serializer.endElement(SoldiersModel.XML_NAMESPACE, "item", "item");
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	serializer.endElement(SoldiersModel.XML_NAMESPACE, "list", "list");
    	serializer.endDocument();
	}

}
