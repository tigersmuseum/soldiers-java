package soldiers.mentions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import soldiers.utilities.CandidateDetails;
import soldiers.utilities.XmlUtils;

public class MakeMentionsCsv {

	public static void main(String[] args) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException, ParseException {

		/*
		 * Run this from source.xml? Fix the output name to be "mentions.csv" in the same directory as input?
		 */
    	if ( args.length < 3 ) {
    		
    		System.err.println("Usage: MakeMentionsCsv <input-filename> <output-filename> <tag>");
    		System.exit(1);
    	}

		XmlUtils xmlutils = new XmlUtils();

		String inputfile = args[0]; String outputfile = args[1]; String tag = args[2];

		Document doc = xmlutils.readDocument(new FileInputStream(inputfile));			 
		doc.normalize();

		Map<Long, Set<Element>> lookup = CandidateDetails.getSoldierMap(doc);
		
		System.out.println("\nSource: " + tag);
		
		Report.reportMentions(lookup);		
		PrintWriter output = new PrintWriter(outputfile);

		for (Long sid: lookup.keySet() ) {
			
			output.println(tag + "," + sid + ",");
		}
		
		output.close();
	}
}
