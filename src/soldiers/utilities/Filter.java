package soldiers.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public class Filter {

	public static void main(String[] args) throws FileNotFoundException, TransformerException {

    	if ( args.length < 4 ) {
    		
    		System.err.println("Usage: Filter <input-file> <output-identified> <output-ambiguous> <output-unknown>");
    		System.exit(1);
    	}
    	
    	String repo = System.getProperty("soldiers.repository");
    	
    	if ( repo == null ) {
    		
    		System.err.println("Set system property 'soldiers.repository' to the location of the Soldiers GitHub repository in the filesystem.");
    		System.exit(1);
    	}
    	
    	String inputfile        = args[0];
    	String outputIdentified = args[1];
    	String outputAmbig      = args[2];
    	String outputUnknown    = args[3];
    	
    	XmlUtils xmlutils = new XmlUtils();
		Document doc = xmlutils.parse(new File(inputfile));
		
		TransformerFactory tf = TransformerFactory.newInstance();
		
		System.out.println("Filtering " + inputfile + " ... ");
		
        Transformer transformer = tf.newTransformer(new StreamSource(repo + "/format/xsl/filter-identified.xsl"));
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new FileOutputStream(outputIdentified));	
		transformer.transform(source, result);
		
        transformer = tf.newTransformer(new StreamSource(repo + "/format/xsl/filter-ambiguous.xsl"));
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
		result = new StreamResult(new FileOutputStream(outputAmbig));	
		transformer.transform(source, result);
		
        transformer = tf.newTransformer(new StreamSource(repo + "/format/xsl/filter-unknown.xsl"));
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
		result = new StreamResult(new FileOutputStream(outputUnknown));	
		transformer.transform(source, result);
		
		doc = xmlutils.parse(new File(outputIdentified));
		source = new DOMSource(doc);
		transformer = tf.newTransformer(new StreamSource(repo + "/format/xsl/filter-report.xsl"));
		transformer.setParameter("bucket", "IDENTIFIED");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
		result = new StreamResult(System.out);	
		transformer.transform(source, result);
		System.out.println("\n");
		
		doc = xmlutils.parse(new File(outputAmbig));
		source = new DOMSource(doc);
		transformer.setParameter("bucket", "AMBIGUOUS");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.transform(source, result);
		System.out.println("\n");
		
		doc = xmlutils.parse(new File(outputUnknown));
		source = new DOMSource(doc);
		transformer.setParameter("bucket", "UNKNOWN");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.transform(source, result);
		System.out.println("\n");
	}

}
