package soldiers.utilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
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

import soldiers.database.Person;
import soldiers.database.SoldiersModel;

public class AmbigCsv {

	public static void main(String[] args) throws IOException, TransformerConfigurationException, IllegalArgumentException, SAXException {

		String line;
		BufferedReader reader  = new BufferedReader(new FileReader("J:\\Archive\\Database\\eclipse-workspace\\Tigers\\scratch-db\\ambig.txt"));
		Map<String, Set<String>> equivalence = new HashMap<String, Set<String>>();
		
		while ( (line = reader.readLine()) != null ) {
			
			String[] fields = line.split(",");
			
			if ( fields[0].matches("\\d+") && fields[0].matches("\\d+") ) {
				
				System.out.println(line);
				addToMap(equivalence, fields[0], fields[1]);
				addToMap(equivalence, fields[1], fields[0]);
			}
		}
		
		reader.close();
		
		Set<Set<String>> partitions = new HashSet<Set<String>>();
		
		for ( String key: equivalence.keySet() ) {
			
			Set<String> partition = new HashSet<String>();
			partition.add(key);
			partition.addAll(equivalence.get(key));
			partitions.add(partition);
		}
		
		System.out.println(partitions);
		serializePartitions(partitions);
	}
	
	public static void addToMap(Map<String, Set<String>> map, String key, String value) {
		
		Set<String> values = map.get(key);
		if ( values == null )  values = new HashSet<String>();
		values.add(value);
		map.put(key, values);
	}
	
	
	private static void serializePartitions(Set<Set<String>> partitions) throws SAXException, TransformerConfigurationException, IllegalArgumentException, FileNotFoundException {
		
		Connection connection = ConnectionManager.getConnection();
		
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
        serializer = tf.newTransformerHandler();
        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setResult(new StreamResult(new FileOutputStream("output/results.xml")));

		serializer.startDocument();
		serializer.startElement(SoldiersModel.XML_NAMESPACE, "list", "list", new AttributesImpl());
		
		for ( Set<String> partition: partitions ) {
			
			serializer.startElement(SoldiersModel.XML_NAMESPACE, "equiv", "equiv", new AttributesImpl());
			
			for ( String sid: partition ) {
				
				Person p = SoldiersModel.getPerson(connection, Long.parseLong(sid));
				p.serializePerson(serializer);				
			}
			serializer.endElement(SoldiersModel.XML_NAMESPACE, "equiv", "equiv");
		}
	
    	serializer.endElement(SoldiersModel.XML_NAMESPACE, "list", "list");
    	serializer.endDocument();
	}
}
