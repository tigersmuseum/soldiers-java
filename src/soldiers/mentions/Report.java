package soldiers.mentions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soldiers.database.MentionsModel;
import soldiers.database.SoldiersNamespaceContext;
import soldiers.utilities.ConnectionManager;
import soldiers.utilities.XmlUtils;

public class Report {

	static XPathFactory factory = XPathFactory.newInstance();
    static XPath xpath = factory.newXPath();

	public static void main(String[] args) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {

		XmlUtils xmlutils = new XmlUtils();	
		Connection connection = ConnectionManager.getConnection();
		
		Set<String> sourcesDB = MentionsModel.getSources(connection);
		System.out.println("Sources in the database: " + sourcesDB);
		
		Document doc = xmlutils.readDocument(new FileInputStream("/C:/Users/Archive.SERLESHOUSE/eclipse-workspace/Research/data/sources.xml"));			 
		doc.normalize();
		Set<String> sourcesXML = Report.getSourceSet(doc);
		System.out.println("Sources in master list: " + sourcesXML);	
		
		sourcesXML.removeAll(sourcesDB);
		System.out.println("Sources not in database: " + sourcesXML);	
	}

	public static Set<String> getSourceSet(Document doc) throws XPathExpressionException {
		
	/*
	 * Returns the set of Soldier IDs from candidate elements in the input XML document.
	 */
		Set<String> sources = new HashSet<String>();
		
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
		
		xpath.setNamespaceContext(namespaceContext);
		XPathExpression sourceExpr = xpath.compile("//source");

		NodeList list = (NodeList) sourceExpr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int i = 0; i < list.getLength(); i++ ) {

			Element source = (Element) list.item(i);
			sources.add(source.getAttribute("name"));
		}
		
		return sources;
	}

}
