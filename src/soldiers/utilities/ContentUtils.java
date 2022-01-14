package soldiers.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class ContentUtils {

	static XPathFactory factory = XPathFactory.newInstance();
    static XPath xpath = factory.newXPath();

    public static void main(String[] args) throws XPathExpressionException, FileNotFoundException, TransformerException {
		
		//contentDates();
		amot();
	}

    
    public static void amot() throws XPathExpressionException, FileNotFoundException, TransformerException {
    	
		XmlUtils xmlutils = new XmlUtils();
		
		Document ref = xmlutils.parse(new File("/C:/workspaces/development/Tigers/output/tempamot.xml"));			 

		XPathExpression expr = xpath.compile(".//page");			
		NodeList list = (NodeList) expr.evaluate(ref.getDocumentElement(), XPathConstants.NODESET);

    	Map<String,String> lookup = new HashMap<String,String>();
    	
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Element e = (Element) list.item(i);
			String source = e.getAttribute("ref");
			String record = e.getAttribute("record");
			System.out.println(source + " = " + record);
			lookup.put(source, record);
		}
		
		Document doc = xmlutils.parse(new File("output/delete.xml"));			 

		xpath.setNamespaceContext(new SoldiersNamespaceContext());
		expr = xpath.compile(".//soldiers:note");
		list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);

		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Element e = (Element) list.item(i);
			String source = e.getAttribute("sourceref");
			String record = lookup.get(source);
			if (record == null) {
				System.err.println("unknown page: " + source);
			}
			else e.setAttribute("amot", "025/" + record);			
		}	
		
		XmlUtils.writeDocument(doc, new FileOutputStream("output/amot.xml"));
	}
    
    
    public static void contentDates() throws XPathExpressionException, FileNotFoundException, TransformerException {
    	
		
		XmlUtils xmlutils = new XmlUtils();
		
    	String filenameIn = "/D:/Tigers/database/CASLDGR/casualty-ledger-PP.xml";
		Document doc = xmlutils.parse(new File(filenameIn));			 
		doc.normalizeDocument(); doc.getDocumentElement();
		
		xpath.setNamespaceContext(new SoldiersNamespaceContext());
		XPathExpression expr = xpath.compile(".//soldiers:note");			
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		ContentUtils.getDateFromContent(list);
		
		XmlUtils.writeDocument(doc, new FileOutputStream("output/delete.xml"));
    }

    
    public static void getDateFromContent(NodeList list) {
    	
		Pattern date = Pattern.compile(".*?(\\d+\\.\\d+\\.\\d+)", Pattern.CASE_INSENSITIVE);
   	
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Element e = (Element) list.item(i);
			String text = e.getTextContent().trim();
			Matcher matcher = date.matcher(text);
			
			if ( matcher.matches() ) {
				
				String match = matcher.group(1);
				String[] x = match.split("\\.");
				
				if ( x.length == 3 ) {
					
					int y = Integer.valueOf(x[2]);
					int m = Integer.valueOf(x[1]);
					int d = Integer.valueOf(x[0]);
					e.setAttribute("date", String.format("%04d-%02d-%02d", y, m, d));
				}
			}
		}
    }

}
