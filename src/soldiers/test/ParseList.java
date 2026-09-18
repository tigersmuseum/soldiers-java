package soldiers.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

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
		testX(inputFile);
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
	
	public static void testX(File inputFile) throws IOException, SAXException {
		
		Set<String> names = getNames(inputFile);
		List<Person> list = new ArrayList<Person>();
		
		// Create a Person object for each name in the input list 
		// We want to store and retrieve names, initials etc. as they are in the raw text (for the time being) - so set normalize flag accordingly
		
		for ( String name: names ) {
			
			Person p = new Person();
			p.setNormalize(false);
			Service service = new Service();
			p.addService(service);
			p.setSurfaceText(StringUtils.normalizeSpace(name));
			list.add(p);
		}
		
		// Process the list to parse the surface text. We can make multiple attempts.
		
		// First, find rank and/or number
		
		for ( Person person: list ) {
			
			String text = person.getSurfaceText();
			String rank = Parser.rankFind(text);
			String number = Parser.numberFind(text);

			// Should be one, and only one, Service record
			Service service = person.getService().iterator().next();
			
			if ( number != null )  service.setNumber(number);
			
			if ( rank != null ) {
			
				service.setRank(rank);
			}
			else {
				
				System.out.println("NO RANK: " + text);				
			}
		}	
		
		// Next, find title
		
		for ( Person person: list ) {
			
			String text = person.getSurfaceText();
			String title = Parser.titleFind(text);
			
			if ( title != null )  person.setTitle(title);
		}
		
		// Next, find suffix
		
		for ( Person person: list ) {
			
			String text = person.getSurfaceText();
			String suffix = Parser.suffixFind(text);
			
			if ( suffix != null )  person.setSuffix(suffix);
		}
		
		// Next, find initials
		
		for ( Person person: list ) {
			
			String text = person.getSurfaceText();
			Service service = person.getService().iterator().next();
			text = removeFromText(text, service.getRank());
			text = removeFromText(text, service.getNumber());
			text = removeFromText(text, person.getSuffix());
			text = removeFromText(text, person.getTitle());

			String initials = Parser.initialsFind(text);
			if ( initials != null ) person.setInitials(initials);
		}
		
		// Next, find surname
		
		for ( Person person: list ) {
			
			String text = person.getSurfaceText();
			Service service = person.getService().iterator().next();
			text = removeFromText(text, service.getRank());
			text = removeFromText(text, service.getNumber());
			text = removeFromText(text, person.getSuffix());
			text = removeFromText(text, person.getInitials());
			text = removeFromText(text, person.getTitle());

			String surname = Parser.surnameFind(text);
			if ( surname != null ) person.setSurname(surname);
		}

		serializeList(list);
	}
	
	public static String removeFromText(String text, String fragment) {
		
		String result = text;
		if ( fragment != null )  result = text.replaceAll(fragment, "").trim();
		return result;
	}
	
	public static Set<String> getNames(File inputFile) throws IOException {
		
		Set<String> names = new HashSet<>();
		names.addAll(FileUtils.readLines(inputFile));
		return names;
	}
	
	public static void testInitials(File inputFile) throws IOException {
		
		List<String> lines = FileUtils.readLines(inputFile);
		
		for ( String line: lines ) {
			
			String initials = Parser.numberFind(line);
			System.out.println("initials: " + line + " = " + initials);
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
		String description = "The Hampshire Regimental Journal, December 1914, Casualties to December 1st, 1914";
		attr.addAttribute("", "src",  "src", "String",  description);
		//attr.addAttribute("", "src",  "src", "String",  "The Hampshire Regimental Journal, May 1946, Nominal Roll of Officers serving with the 2nd Battalion on VE-Day");
		serializer.startElement(SoldiersModel.XML_NAMESPACE, "list", "list", new AttributesImpl());
		serializer.startElement(SoldiersModel.XML_NAMESPACE, "description", "description", new AttributesImpl());
		serializer.characters(description.toCharArray(), 0, description.length());
		serializer.endElement(SoldiersModel.XML_NAMESPACE, "description", "description");

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
