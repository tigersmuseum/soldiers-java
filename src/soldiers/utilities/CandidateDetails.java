package soldiers.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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

import soldiers.database.SoldiersNamespaceContext;

public class CandidateDetails {

	static XPathFactory factory = XPathFactory.newInstance();
    static XPath xpath = factory.newXPath();

	public static void main(String[] args) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {

    	if ( args.length < 2 ) {
    		
    		System.err.println("Usage: Soldiers <input-filename> <output-filename>");
    		System.exit(1);
    	}
    	
    	String inputfile  = args[0];
    	String outputfile = args[1];
    	
		System.out.printf("input file:  %s\n", inputfile);
		System.out.printf("output file: %s\n\n", outputfile);

		Document doc = Soldiers.readDocument(new FileInputStream(inputfile));			 
		doc.normalize();

		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
		
		xpath.setNamespaceContext(namespaceContext);
		XPathExpression expr1 = xpath.compile("//soldiers:person");
		XPathExpression expr2 = xpath.compile(".//soldiers:candidate[1]");
		NodeList list = (NodeList) expr1.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int i = 0; i < list.getLength(); i++ ) {

			Element person = (Element) list.item(i);
			
			Element candidate = (Element) expr2.evaluate(person, XPathConstants.NODE);
			
			if ( candidate != null) {
				
				long sid = Long.parseLong(candidate.getAttribute("sid"));
				Document pdoc = Report.getPersonDOM(sid);
				pdoc.getDocumentElement().appendChild(pdoc.importNode(candidate, true));
				
				Element parent = (Element) person.getParentNode();
				parent.removeChild(person);
	        	parent.appendChild(doc.importNode(pdoc.getDocumentElement(), true));
			}

		}
		
		Soldiers.writeDocument(doc, new FileOutputStream(outputfile));
	}

}
