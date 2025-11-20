package soldiers.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

	public static void main(String[] args) throws XPathExpressionException, FileNotFoundException, TransformerException, ParseException {

		SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat d2 = new SimpleDateFormat("y-D");
		SimpleDateFormat d3 = new SimpleDateFormat("d MMM yyyy");
		
		//Date start = d1.parse("1944-06-01");

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
		xpath.setNamespaceContext(namespaceContext);

		XmlUtils utils = new XmlUtils();
		//File input = new File("/C:/Users/Archive/Documents/GitHub/history/events/rhants/eventdiary.xhtml");
		File input = new File("/D:/GitHub/history/units/hants.xhtml");
		Document doc = utils.parse(input);
		
		//XPathExpression expr = xpath.compile(".//xhtml:span[ancestor::xhtml:td[1][contains(@class, 'date')]]");
		XPathExpression expr = xpath.compile(".//xhtml:datetime");

		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		System.out.println(list.getLength());
		
		for (int i = 0; i < list.getLength(); i++) {

			Element e = (Element) list.item(i);
			try {
				//Date date = d1.parse(e.getAttribute("content"));
				Date date = d1.parse(e.getTextContent());
				//e.setAttributeNS("", "ordinal", d2.format(date));
				e.setAttributeNS("", "content", e.getTextContent());
				e.setTextContent(d3.format(date));
				//e.setAttributeNS("", "offset", TimeUnit.DAYS.convert(date.getTime() - start.getTime(), TimeUnit.MILLISECONDS));
				//long x = TimeUnit.DAYS.convert(date.getTime() - start.getTime(), TimeUnit.MILLISECONDS);
				//System.out.println(x);
				//e.setAttributeNS("", "offset", String.valueOf(x));
			}
			catch (ParseException e1) {
				// do nothing
			}
		}
		
		XmlUtils.writeDocument(doc, new FileOutputStream("output/events.xhtml"));
		
		/*
		 SimpleDateFormat myFormat = new SimpleDateFormat("dd MM yyyy");
String inputString1 = "23 01 1997";
String inputString2 = "27 04 1997";

try {
    Date date1 = myFormat.parse(inputString1);
    Date date2 = myFormat.parse(inputString2);
    long diff = date2.getTime() - date1.getTime();
    System.out.println ("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
} catch (ParseException e) {
    e.printStackTrace();
}
		 */
	}

}
