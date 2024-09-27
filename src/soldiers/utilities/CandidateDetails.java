package soldiers.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

	
	public static Set<Long> getSoldierSet(Document doc) throws XPathExpressionException {
		
	/*
	 * Returns the set of Soldier IDs from candidate elements in the input XML document.
	 */
		Set<Long> soldiers = new HashSet<Long>();
		
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
		
		xpath.setNamespaceContext(namespaceContext);
		XPathExpression soldierExpr = xpath.compile("//soldiers:person");
		XPathExpression candidateExpr = xpath.compile(".//soldiers:candidate[1]");
		NodeList list = (NodeList) soldierExpr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int i = 0; i < list.getLength(); i++ ) {

			Element person = (Element) list.item(i);
			
			Element candidate = (Element) candidateExpr.evaluate(person, XPathConstants.NODE);
			
			if ( candidate != null ) {
				
				long sid = Long.parseLong(candidate.getAttribute("sid"));
				soldiers.add(sid);
			}
		}
		
		return soldiers;
	}

	
	public static Map<Long, Set<Element>> getSoldierMap(Document doc) throws XPathExpressionException {
		
	/*
	 * Returns a map of Soldier ID to person elements (with a candidate element pointing to that ID) in the input XML document.
	 */
		Map<Long, Set<Element>> lookup = new HashMap<Long, Set<Element>>();
		
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
		
		xpath.setNamespaceContext(namespaceContext);
		XPathExpression soldierExpr = xpath.compile("//soldiers:person");
		XPathExpression candidateExpr = xpath.compile(".//soldiers:candidate[1]");
		NodeList list = (NodeList) soldierExpr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int i = 0; i < list.getLength(); i++ ) {

			Element person = (Element) list.item(i);
			
			Element candidate = (Element) candidateExpr.evaluate(person, XPathConstants.NODE);
			
			if ( candidate != null ) {
				
				long sid = Long.parseLong(candidate.getAttribute("sid"));
				Set<Element> candidates = lookup.get(sid);
				if ( candidates == null )  candidates = new HashSet<Element>();
				candidates.add(person);
				lookup.put(sid, candidates);
			}
		}
		
		return lookup;
	}
}
