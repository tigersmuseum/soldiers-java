package soldiers.mentions;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import soldiers.database.SoldiersNamespaceContext;

public class Sources {


	static XPathFactory factory = XPathFactory.newInstance();
    static XPath xpath = factory.newXPath();


	public static Map<String, String> getSourceMap(Document doc) throws XPathExpressionException {
		
	/*
	 * Returns the map of Soldier IDs to filename from Sources XML.
	 */
		Map<String, String> sources = new HashMap<String, String>();
		
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
		
		xpath.setNamespaceContext(namespaceContext);
		XPathExpression sourceExpr = xpath.compile("//source");

		NodeList list = (NodeList) sourceExpr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int i = 0; i < list.getLength(); i++ ) {

			Element source = (Element) list.item(i);
			sources.put(source.getAttribute("name"), source.getAttribute("file"));
		}
		
		return sources;
	}
}
