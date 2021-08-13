package soldiers.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soldiers.database.SoldiersModel;
import soldiers.utilities.ConnectionManager;
import soldiers.utilities.HtmlUtils;

public class HtmlData {

	public static void main(String[] args) throws XPathExpressionException, IllegalArgumentException, FileNotFoundException, SAXException, TransformerException {

		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

		HtmlUtils html = new HtmlUtils();
		
		File file = new File(args[0]);
		Document doc = html.cleanFile(file);
		
		XPathExpression expr = xpath.compile("//tr");
		NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		
		System.out.println(list.getLength());
		
		writeXml(doc);
		
		long sid = SoldiersModel.getNextAvailableSoldierId(ConnectionManager.getConnection());
		System.out.println(sid);

	}

	
	public static void writeXml(Document doc) throws SAXException, IllegalArgumentException, FileNotFoundException, TransformerException {
		
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
        serializer = tf.newTransformerHandler();
        Transformer transformer = serializer.getTransformer();
        
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD XHTML 1.0 Transitional//EN");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");

        DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new FileOutputStream("output/htmldata.xml"));
		transformer.transform(source, result);
	}

}
