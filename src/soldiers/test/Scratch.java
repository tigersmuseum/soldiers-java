package soldiers.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soldiers.database.SoldiersNamespaceContext;
import soldiers.utilities.XmlUtils;

public class Scratch {

	public static void main(String[] args) throws XPathExpressionException, IllegalArgumentException, FileNotFoundException, SAXException, ParseException, TransformerException {

		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    XmlUtils xmlutils = new XmlUtils();
		Document doc = xmlutils.parse(new File("output/14star.xml"));
				
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();

        xpath.setNamespaceContext(namespaceContext);
		XPathExpression expr = xpath.compile("//soldiers:disembark|//soldiers:transfer");
		
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		addParsedDateAttribute(list);
		
		TransformerFactory treansformerFactory = TransformerFactory.newInstance();
		Transformer transformer = treansformerFactory.newTransformer();
		StreamResult result = new StreamResult(new FileOutputStream("output/fixed.xml"));
		
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);      
	}
	
	
	public static void addParsedDateAttribute(NodeList list) {
		
        Pattern datePattern = Pattern.compile(".*?(\\d{1,2})\\.(\\d{1,2})\\.(\\d{2}).*");
        Pattern diedPattern = Pattern.compile(".*?(dead|died|kia|dow).*", Pattern.CASE_INSENSITIVE);
        Pattern disPattern  = Pattern.compile(".*?(Dis|MU|TE).*");
        Pattern xferPattern = Pattern.compile(".*?(trans).*", Pattern.CASE_INSENSITIVE);
        Pattern dsrtPattern = Pattern.compile(".*?(desert).*", Pattern.CASE_INSENSITIVE);

        for ( int i = 0; i < list.getLength(); i++ ) {
			
			Element e = (Element) list.item(i);
	        String text = e.getTextContent();
			
			Matcher dateMatcher = datePattern.matcher(text);
			Matcher diedMatcher = diedPattern.matcher(text);
			Matcher disMatcher  = disPattern.matcher(text);
			Matcher xferMatcher = xferPattern.matcher(text);
			Matcher dsrtMatcher = dsrtPattern.matcher(text);

			if ( dateMatcher.matches() ) {
				
				String normal = String.format("19%2d-%02d-%02d", Integer.parseInt(dateMatcher.group(3)), Integer.parseInt(dateMatcher.group(2)), Integer.parseInt(dateMatcher.group(1)));
				System.out.println(normal);
				e.setAttribute("date", normal);
			}

			if ( diedMatcher.matches() ) {
				
				e.setAttribute("died", "yes");
			}

			if ( disMatcher.matches() ) {
				
				e.setAttribute("discharged", "yes");
			}

			if ( xferMatcher.matches() ) {
				
				e.setAttribute("transferred", "yes");
			}

			if ( dsrtMatcher.matches() ) {
				
				e.setAttribute("deserted", "yes");
			}
		}	
	}
	
}
