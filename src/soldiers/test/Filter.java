package soldiers.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

import soldiers.database.Person;
import soldiers.database.SoldiersNamespaceContext;
import soldiers.utilities.Soldiers;
import soldiers.utilities.XmlUtils;

/**
 * Split a file of Soldiers XML, with candidate elements suggesting identity, into 3 output files: One where person elements are uniquely 
 * identified (a single candidate element), one where persons are ambiguous (multiple candidate elements), and one where persons 
 * are unknown (no candidate element);
 * 
 * @author Royal Hampshire Regiment Museum
 *
 */

public class Filter {

	public static void main(String[] args) throws XPathExpressionException, IllegalArgumentException, FileNotFoundException, SAXException, ParseException, TransformerException {

		/*
		 Filters a file of person entries that have been through the identification process. These will have a "candidate" element
		 for each matching entry in the soldiers database.
		 
		 This program fixes the person to the soldier ID if there is exactly one candidate entry, otherwise filters the person entry to
		 either an "ambiguous" file (multiple candidates) or an "unknown" file (no candidates).
		 
		 Quality control on the single candidate?
		 */

    	if ( args.length < 1 ) {
    		
    		System.err.println("Usage: Filter <input-filename>");
    		System.exit(1);
    	}

    	String inputfile = args[0];

		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    SoldiersNamespaceContext namespace = new SoldiersNamespaceContext();
	    
	    XmlUtils xmlutils = new XmlUtils();
		Document doc = xmlutils.parse(new File(inputfile));
		
		// a document for unique identification
		Document iddoc = xmlutils.newDocument();
		Element idroot = iddoc.createElementNS(namespace.getNamespaceURI("soldiers"), "list");
		iddoc.appendChild(idroot);
		
		// a document for ambiguous identification
		Document ambigdoc = xmlutils.newDocument();
		Element ambigroot = ambigdoc.createElementNS(namespace.getNamespaceURI("soldiers"), "list");
		ambigdoc.appendChild(ambigroot);
		
		// a document for unidentified persons
		Document unkdoc = xmlutils.newDocument();
		Element unkroot = unkdoc.createElementNS(namespace.getNamespaceURI("soldiers"), "list");
		unkdoc.appendChild(unkroot);
				
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();

        xpath.setNamespaceContext(namespaceContext);
		XPathExpression expr = xpath.compile("//soldiers:person");
		
		Set<Person> identified = new HashSet<Person>();
		Set<Person> unidentified = new HashSet<Person>();
		Set<Person> ambiguous = new HashSet<Person>();
		
		Map<String, String> setmap = new HashMap<String, String>();
		
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Element e = (Element) list.item(i);
	        Person p = Soldiers.parsePerson(e);
	        
	        NodeList clist = e.getElementsByTagNameNS(namespaceContext.getNamespaceURI("soldiers"), "candidate");
	        
	        if ( clist.getLength() == 1 ) {
	        	
	        	Element candidate = (Element) clist.item(0);
	        	p.setSoldierId(Long.valueOf(candidate.getAttribute("sid")));

	        	identified.add(p);
	        	idroot.appendChild(iddoc.importNode(e, true));
	        }
	        else if ( clist.getLength() == 0 ) {
	        	
	        	unidentified.add(p);
	        	unkroot.appendChild(unkdoc.importNode(e, true));
	        }
	        else {
	        	ambiguous.add(p);
	        	ambigroot.appendChild(ambigdoc.importNode(e, true));
	        	
	        	System.out.println(clist.getLength() + " x== "  + p.getContent());
	        	Set<String> names = new HashSet<String>();
	        	
	        	for ( int j = 0; j < clist.getLength(); j++ ) {
	        		
	        		Element c = (Element) clist.item(j);

	        		String key = String.format("%s:%s", c.getAttribute("number"), c.getAttribute("sort"));
	        		names.add(key);
		        	
	        		System.out.println(c.getAttribute("content") + " = " + p.getContent() + "? " + c.getAttribute("content").equals(p.getContent()));
	        	}
	        	
	        	System.out.println("-------------- " + names.size());
	        }
		}

		TransformerFactory treansformerFactory = TransformerFactory.newInstance();
		Transformer transformer = treansformerFactory.newTransformer();
/*		StreamResult result = new StreamResult(new FileOutputStream("output/fixed.xml"));
		
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result); */
		
		System.out.println(setmap);
		System.out.println("IDENTIFIED: " + identified.size());
		System.out.println("UNIDENTIFIED: " + unidentified.size());
		System.out.println("AMBIGUOUS: " + ambiguous.size());
		
		StreamResult idresult = new StreamResult(new FileOutputStream("output/identified.xml"));		
		DOMSource idsource = new DOMSource(iddoc);
		transformer.transform(idsource, idresult);

		StreamResult ambigresult = new StreamResult(new FileOutputStream("output/ambiguous.xml"));		
		DOMSource ambigsource = new DOMSource(ambigdoc);
		transformer.transform(ambigsource, ambigresult);

		StreamResult unkresult = new StreamResult(new FileOutputStream("output/unknown.xml"));		
		DOMSource unksource = new DOMSource(unkdoc);
		transformer.transform(unksource, unkresult);
	}
		
}
