package soldiers.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import soldiers.database.Normalize;
import soldiers.database.Person;
import soldiers.database.SoldiersModel;
import soldiers.text.Parser;

public class ParseList {
	
	public static void main(String[] args) throws IOException, TransformerConfigurationException, SAXException {

    	if ( args.length < 1 ) {
    		
    		System.err.println("Usage: ParseList <filename>");
    		System.exit(1);
    	}
    	
    	String inputfile = args[0];
    	Map<String, Person> individuals = new HashMap<String, Person>();

		FileInputStream inputFile = new FileInputStream(inputfile);

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputFile));
		List<Person> list = new ArrayList<Person>();
		
		String line;

		while ((line = reader.readLine()) != null) {
			
			String text = line.replaceAll("\\p{javaSpaceChar}", " ").trim();
			List<Person> l = Parser.findMention(text);
			list.addAll(l);

			for (Person p: list) {
				
				individuals.put(p.getSurfaceText(), p);
			}
		}
		
		reader.close();
		
		Normalize.normalizeRank(list);
		
/*		for ( Person p: list ) {
			
			System.out.printf("(%d) %s = %s\n", p.getSoldierId(), p.getContent(), p.getSurfaceText());
		}
*/		
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
        serializer = tf.newTransformerHandler();
        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setResult(new StreamResult(new FileOutputStream("output/list.xml")));

		serializer.startDocument();
		serializer.startElement(SoldiersModel.XML_NAMESPACE, "list", "list", new AttributesImpl());

		for ( String text: individuals.keySet() ) {
			
			Person p = individuals.get(text); 
			p.serializePerson(serializer);
		}
		
		serializer.endElement(SoldiersModel.XML_NAMESPACE, "list", "list");
		serializer.endDocument();

	}
	
}
