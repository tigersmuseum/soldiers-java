package soldiers.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import soldiers.database.SoldiersNamespaceContext;

public class Scratch {

	public static void main(String[] args) throws XPathExpressionException, FileNotFoundException, TransformerException {

		SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat d2 = new SimpleDateFormat("y-D");

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
		xpath.setNamespaceContext(namespaceContext);

		XmlUtils utils = new XmlUtils();
		File input = new File("/C:/Users/Archive/Documents/GitHub/history/events/rhants/eventdiary.xhtml");
		Document doc = utils.parse(input);
		
		XPathExpression expr = xpath.compile(".//xhtml:span[ancestor::xhtml:td[1][contains(@class, 'date')]]");

		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		System.out.println(list.getLength());
		
		for (int i = 0; i < list.getLength(); i++) {

			Element e = (Element) list.item(i);
			try {
				Date date = d1.parse(e.getAttribute("content"));
				e.setAttributeNS("", "ordinal", d2.format(date));
			}
			catch (ParseException e1) {
				// do nothing
			}
		}
		
		XmlUtils.writeDocument(doc, new FileOutputStream("output/events.xhtml"));
	}

}
