package soldiers.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.database.SoldiersModel;
import soldiers.text.Normalize;
import soldiers.text.Parser;

public class ParseList {
	
	static Pattern whitespace = Pattern.compile("^|\\s+"); // match the start of the text, or any sequence of whitespace
	public static Pattern companyPattern = Pattern.compile("[A-Z]\\s+Coy"); // match a unit (signals the end of a name string)

	public static void main(String[] args) throws IOException, TransformerConfigurationException, SAXException {


		FileInputStream inputFile = new FileInputStream("C:\\workspaces\\development\\Tigers\\input\\list.txt");

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputFile));
		List<Person> list = new ArrayList<Person>();
		
		String line;

		while ((line = reader.readLine()) != null) {
			
			String text = line.replaceAll("\\p{javaSpaceChar}", " ").trim();
			List<Person> l = findMention(text);
			list.addAll(l);
		}
		
		reader.close();
		
		Normalize.normalizeRank(list);
		
		for ( Person p: list ) {
			
			System.out.printf("(%d) %s = %s", p.getSoldierId(), p.getContent(), p.getSurfaceText());
		}
		
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
        serializer = tf.newTransformerHandler();
        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setResult(new StreamResult(new FileOutputStream("output/results.xml")));

		serializer.startDocument();
		serializer.startElement(SoldiersModel.XML_NAMESPACE, "list", "list", new AttributesImpl());


		for ( Person p: list ) {
			
			p.serializePerson(serializer);
		}
		
		serializer.endElement(SoldiersModel.XML_NAMESPACE, "list", "list");
		serializer.endDocument();

	}
	
	
	public static String trimUnit(String text) {
		
		String retval = text;
		
		Matcher companyMatcher = companyPattern.matcher(text);

		if ( companyMatcher.find() ) {
			
			//String unit = companyMatcher.group(0).trim();
			retval = text.substring(0, companyMatcher.start()).trim();
		}

		return retval;
	}
	
	
	public static List<Person> findMention(String text) {
		
		// Step through the text looking for names. There might be an number of soldiers mentioned, so we create a list.
		List<Person> list = new ArrayList<Person>();

		String remaining = text;
		
		while ( remaining.length() > 0 ) {
			
			Matcher spaceMatcher = whitespace.matcher(remaining);
						
			Service service = new Service();
			StringBuffer surface = new StringBuffer();
			
			// Look for a rank, but expect to possible find service number on the way.
			
			while ( service.getRank() == "UNK" && spaceMatcher.find() ) {
				
				String current = remaining.substring(spaceMatcher.end());
				Parser.numberLookAt(current, service);
				Parser.rank(current, service);				
			}
			
			if ( service.getRank() != "UNK" ) { // we have rank, and we expect to find a name following
				
				surface.append(service.getNumber()); surface.append(' ');
				surface.append(service.getRank()); surface.append(' ');
				
				String nxt = remaining.substring(spaceMatcher.end() + service.getRank().length()).trim();
				
				Matcher nameMatcher = Parser.namePattern.matcher(nxt);
				
				if ( nameMatcher.lookingAt() ) {
					
					Person person = new Person();
					String name = trimUnit(nameMatcher.group(0));
					surface.append(name);
					person.addService(service);
					Parser.name(name, person);
					person.setSurfaceText(surface.toString().replaceAll("//s+", " ").trim());					
					list.add(person);
					
					remaining = nxt.substring(nameMatcher.end()).trim();
				}
				else {
					remaining = nxt.substring(service.getRank().length());
					service = new Service();
				}
			}
			else { // we're finished
				
				remaining = "";
			}

		}
		
		return list;
	}
}
