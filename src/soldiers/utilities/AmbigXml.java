package soldiers.utilities;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AmbigXml {

	public static void main(String[] args) throws IOException, TransformerConfigurationException, IllegalArgumentException, SAXException, XPathExpressionException {


    	if ( args.length < 1 ) {
    		
    		//System.err.println("Usage: AmbigXml <input-filename> <output-filename>");
    		System.err.println("Usage: AmbigXml <input-filename>");
    		System.exit(1);
    	}
		
     	String inputfile   = args[0];
    	//String outputfile  = args[1];

		XmlUtils xmlutils = new XmlUtils();
		
		Set<Set<String>> partitions = new HashSet<Set<String>>();
		
		Document source = xmlutils.parse(new File(inputfile));
		XPath xpath = xmlutils.newXPath();
		XPathExpression personExpr = xpath.compile("//soldiers:person");
		XPathExpression candidateExpr = xpath.compile(".//soldiers:candidate");
		
		NodeList people = (NodeList) personExpr.evaluate(source.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int p = 0; p < people.getLength(); p++ ) {
			
			Element person = (Element) people.item(p);

			NodeList list = (NodeList) candidateExpr.evaluate(person, XPathConstants.NODESET);
			
			Set<String> candidates = new HashSet<String>();
			
			for ( int i = 0; i < list.getLength(); i++ ) {
				
				Element e = (Element) list.item(i);
				long sid = Long.parseLong(e.getAttribute("sid"));
				candidates.add(String.valueOf(sid));
			}
			
			if ( candidates.size() > 1 )   partitions.add(candidates);
		}
				
		System.out.println(partitions);
		AmbigCsv.serializePartitions(partitions);
	}

}
