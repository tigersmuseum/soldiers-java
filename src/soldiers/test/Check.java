package soldiers.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Date;
import java.text.ParseException;

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
import soldiers.database.SoldiersModel;
import soldiers.database.SoldiersNamespaceContext;
import soldiers.utilities.ConnectionManager;
import soldiers.utilities.Soldiers;
import soldiers.utilities.XmlUtils;

public class Check {

	public static void main(String[] args) throws XPathExpressionException, IllegalArgumentException, FileNotFoundException, SAXException, ParseException, TransformerException {

		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    XmlUtils xmlutils = new XmlUtils();
		Document doc = xmlutils.parse(new File("output/out.xml"));
				
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();

        xpath.setNamespaceContext(namespaceContext);
		XPathExpression expr = xpath.compile("//soldiers:person");
		
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Element e = (Element) list.item(i);
	        Person p = Soldiers.parsePerson(e);
	        
	        NodeList clist = e.getElementsByTagNameNS(namespaceContext.getNamespaceURI("soldiers"), "candidate");
	        
	        if ( clist.getLength() == 1 ) {
	        	
	        	Element candidate = (Element) clist.item(0);
	        	
	        	//System.out.println(candidate.getAttribute("content") + " == "  + p.getContent() + " = " + p.getContent().equals(candidate.getAttribute("content")));
	        	
	        	p.setSoldierId(Long.valueOf(candidate.getAttribute("sid")));
	        	
	        	//checkTransfer(xpath, e, p);
	        }
	        else {
	        	System.out.println(clist.getLength() + " x== "  + p.getContent());
	        	
	        }
		}

		TransformerFactory treansformerFactory = TransformerFactory.newInstance();
		Transformer transformer = treansformerFactory.newTransformer();
		StreamResult result = new StreamResult(new FileOutputStream("output/fixed.xml"));
		
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);      
	}
	
	
	public static void checkTransfer(XPath xpath, Element p, Person person) throws XPathExpressionException {
		
		XPathExpression expr = xpath.compile(".//soldiers:transfer[@date][@died][1]");
		Element t = (Element) expr.evaluate(p, XPathConstants.NODE);
		
		if (t != null) {
			//System.out.println("****** " + t.getAttribute("date"));
			person.setDeath(Date.valueOf(t.getAttribute("date")));
			
			Person known = SoldiersModel.getPerson(ConnectionManager.getConnection(), person.getSoldierId());
			if ( !person.getDeath().equals(known.getDeath())) System.out.println(person.getSoldierId() + " ..  " + known.getDeath() + " != " + person.getDeath());
			
			if ( known.getDeath() == null && person.getDeath() != null ) {
				
				known.setDeath(person.getDeath());
				SoldiersModel.updateDeath(ConnectionManager.getConnection(), known);
			}
		}
	}
}
