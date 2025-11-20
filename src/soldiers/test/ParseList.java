package soldiers.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.io.FileUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import soldiers.database.Normalize;
import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.database.SoldiersModel;
import soldiers.text.Parser;
import soldiers.utilities.XmlUtils;

public class ParseList {
	
	public static void main(String[] args) throws IOException, TransformerConfigurationException, SAXException {

    	if ( args.length < 1 ) {
    		
    		System.err.println("Usage: ParseList <filename>");
    		System.exit(1);
    	}
    	
    	String inputfile = args[0];

		File inputFile = new File(inputfile);
		//canonicalList(inputFile);
		testInitials(inputFile);
		//Normalize.normalizeRank(list);
	}
	
	public static void canonicalList(File inputFile) throws IOException, SAXException {
		
		List<Person> list = findList(inputFile);
		dittoRank(list);
		output2(list);
		serializeList(list);
	}
	
	public static void mentionList(File inputFile) throws IOException, SAXException {
		
		Map<String, Person> individuals = new HashMap<String, Person>();
		findMentions(inputFile, individuals);
		output1(individuals);
	}
	
	public static void testInitials(File inputFile) throws IOException {
		
		List<String> lines = FileUtils.readLines(inputFile);
		
		for ( String line: lines ) {
			
			String initials = Parser.initialsFind(line);
			System.out.println(line + " = " + initials);
		}
	}
	
	public static List<Person> findList(File inputFile) throws IOException {
		
		List<Person> list = new ArrayList<Person>();	
		List<String> lines = FileUtils.readLines(inputFile);

		for ( String line: lines ) {
			
			String text = line.replaceAll("\\p{javaSpaceChar}", " ").trim();
			Person p = Parser.parseCanonical(text);
			p.setSurfaceText(text);
			list.add(p);
		}
		
		return list;
	}
	
	public static void findMentions(File inputFile, Map<String, Person> individuals) throws IOException {
		
		List<Person> list = new ArrayList<Person>();	
		List<String> lines = FileUtils.readLines(inputFile);

		for ( String line: lines ) {
			
			String text = line.replaceAll("\\p{javaSpaceChar}", " ").trim();
			System.out.println(text);
			List<Person> l = Parser.findMention(text);
			list.addAll(l);

			for (Person p: list) {
				
				individuals.put(p.getSurfaceText(), p);
			}
		}
	}
	
	public static void output1(Map<String, Person> individuals) throws FileNotFoundException, SAXException {
		
        ContentHandler serializer = XmlUtils.getSerializer(new FileOutputStream("output/list.xml"));
		serializer.startDocument();
		serializer.startElement(SoldiersModel.XML_NAMESPACE, "list", "list", new AttributesImpl());

		for ( String text: individuals.keySet() ) {
			
			Person p = individuals.get(text); 
			p.serializePerson(serializer);
		}
		
		serializer.endElement(SoldiersModel.XML_NAMESPACE, "list", "list");
		serializer.endDocument();
	}
	
	
	public static void output2(List<Person> soldiers) {
		
		for ( Person person: soldiers ) {
			
			System.out.println(person.getSurfaceText() + " = " + person.getContent());
		}
	}
	
	
	public static void serializeList(List<Person> list) throws FileNotFoundException, SAXException {
		
        ContentHandler serializer = XmlUtils.getSerializer(new FileOutputStream("output/list.xml"));
		serializer.startDocument();
		AttributesImpl attr = new AttributesImpl();
		attr.addAttribute("", "src",  "src", "String",  "The Hampshire Regimental Journal, January 1919, Killed in Action");
		serializer.startElement(SoldiersModel.XML_NAMESPACE, "list", "list", attr);

		for ( Person p: list ) {
			
			p.serializePerson(serializer);
		}
		
		serializer.endElement(SoldiersModel.XML_NAMESPACE, "list", "list");
		serializer.endDocument();
	}
	
	public static void dittoRank(List<Person> soldiers) {
		
		String rank = soldiers.get(0).getService().iterator().next().getRank();
		Iterator<Person> iterator = soldiers.iterator();
		
		while ( iterator.hasNext() ) {
			
			Person person = iterator.next();
			Service service = person.getService().iterator().next();
			String currentRank = service.getRank();
			
			if ( currentRank.equals("UNK") )  service.setRank(rank);
			else rank = currentRank;
			
			service.setRegiment("Hampshire Regiment");
			service.setBefore(Date.valueOf("1918-05-31"));
		}
	}
}
