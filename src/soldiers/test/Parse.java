package soldiers.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;
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
import soldiers.utilities.Parser;
import soldiers.utilities.XmlUtils;

public class Parse {

	public static void main(String[] args) throws XPathExpressionException, TransformerConfigurationException, IllegalArgumentException, FileNotFoundException, SAXException {

		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    XmlUtils xmlutils = new XmlUtils();
	    String inputfile = args[0];
	    if ( inputfile == null ) {
	    	
	    	System.err.println("Parse <filename>");
	    	System.exit(-1);
	    }
	    
	    Normalize normalizer = new Normalize();
	    Map<String, String> ranks = normalizer.getRanks();
	    
		Document doc = xmlutils.parse(new File(inputfile));
		
		XPathExpression expr = xpath.compile("//accession");
		NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		
		System.out.println(list.getLength());
		
		Set<Person> collection = new HashSet<Person>();
    	
		for ( int i = 0; i < list.getLength(); i++ ) {
		
			Element e = (Element) list.item(i);
			//String text = e.getAttribute("person");
			//String set = e.getAttribute("number");
			Element eh = (Element) e.getElementsByTagName("history").item(0);
			String text = null;		
			if (eh.getElementsByTagName("p").getLength() != 0) text = eh.getElementsByTagName("p").item(0).getTextContent();
			String inputtext = text;

			if ( text != null ) {
							
				//text = text.toUpperCase();

				text = text.replaceAll("\\p{javaSpaceChar}", " ").trim();
				text = text.replaceAll(":", " ");
				text = text.replaceAll("\\s+", " ");
				text = text.replaceAll("(?i).*?medal(s)? of", "");
				text = text.replaceAll("(?i).*?decoration(s)? of", "");
				text = text.replaceAll("(?i)^gift", "");
				text = text.replaceAll("(?i)^T\\.S\\.\\s", "");
				text = text.replaceAll("(?i)^.*?the late", "");
				text = text.replaceAll("(?i)^.*?belong(ing|ed) to", "");
				text = text.replaceAll("(?i)^.*?awarded to", "");
				text = text.replaceAll("(?i)^.*?(those|property|artefacts) of", "");
				text = text.replaceAll("(?i)^.*?\\[\\d+\\]", "");
				text = text.replaceAll("(?i)(served).*", "");
				text = text.replaceAll("(?i)([-—]\\s+)?(of\\s+)?(the\\s+)?(\\d+(ST|ND|RD|TH)?\\s*)(HAMP|HANT|37TH|67TH|BATT|BN|VOL).*", "");
				text = text.replaceAll("(?i)(of\\s+)?(the\\s+)?(HAMP|HANT|HYEO|TR HAMP|WEST RIDING|37TH|67TH|VOL).*", "");
				text = text.replaceAll("(?i)(\\s+)?(67/FOOT|1/).*", "");
				text = text.replaceAll("(?i)(\\s+)?(\\().*", "");
				text = text.replaceAll("(?i)(\\s+)?(\\[).*", "");
				text = text.replaceAll("(?i)K\\.I\\.A.*", "");
				text = text.replaceAll("(?i)A\\.S\\.C.*", "");
				text = text.replaceAll("(?i)V\\.C.*", "");
				text = text.replaceAll("(?i)(enlisted|disembarked|entered|attached).*", "");
				text = text.replaceAll("(?i)\\smade into\\s.*", "");
				text = text.replaceAll("(DCM|MM|MC|DSO|VC|CB|OBE|MBE|CBE|ASC)\\b.*", "");
				text = text.replaceAll("\\d+H.*", "");
				text = text.replaceAll("\\d\\/$", "");
				text = text.replaceAll("40B", "40");
				text = text.replaceAll("(?i)2\\/Lt", "2Lt");
				text = text.replaceAll("\\d\\/$", "");
				text = text.replaceAll("(?i)([-—]\\s+)?(\\d\\/)?\\d+(ST|ND|RD|TH).*", "");
				text = text.replaceAll("(?i)\\s(who|born|late|died|was|presented)\\b.*", "");
				text = text.replaceAll(",", " ");
				text = text.replaceAll("H\\.$", "");
				text = text.replaceAll("ASC$", "");
				text = text.trim();
				System.out.println(" ... " + text);
				
				Person p = Parser.parsePersonMention(text);
				System.out.println(text);
				System.out.println(p);
				p.setSurfaceText(inputtext);
				System.out.println("** " + p.getContent());
				
				normalizer.normalizeRank(p, ranks);
				collection.add(p);
			}
		}
		
		XmlUtils.writeXml(collection, new FileOutputStream("output/out.xml"));
	}

}
