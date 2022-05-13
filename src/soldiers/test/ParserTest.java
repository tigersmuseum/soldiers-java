package soldiers.test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.utilities.Parser;

public class ParserTest {
	
	static Pattern whitespace = Pattern.compile("^|\\s+"); // match the start of the text, or any sequence of whitespace
	public static Pattern companyPattern = Pattern.compile("[A-Z]\\s+Coy"); // match a unit (signals the end of a name string)

	public static void main(String[] args) {

		//String surface = "4. 5827 Pte E Marshall A Coy has been permitted to extend his service to complete 12 years with the Colours. Authority dtd 1.10.06.";
		String surface ="5. Capt FC Moore will take over command & payment of D Coy from Lt JDM Beckett with effect from 22nd inst.";
		String text = surface.replaceAll("\\p{javaSpaceChar}", " ").trim();
				
		Matcher spaceMatcher = whitespace.matcher(text);
		
		Person person = new Person();
		Service service = new Service();
		
		// Step through the text looking for names. There might be an number of soldiers mentioned, so we create a list
		
		List<Person> list = new ArrayList<Person>();
		
		while ( spaceMatcher.find() ) {
			
			String current = text.substring(spaceMatcher.end());
			//System.out.println(current);
			
			// If we have a rank, we expect it to be followed by a name. Lots of ways this might be expressed, so start
			// by trying to find a run of text that captures any potential initials, forenames and surnames.
			
			if ( service.getRank() != "UNK" && person.getSurfaceText() == null ) {
				
				Matcher nameMatcher = Parser.namePattern.matcher(text.substring(spaceMatcher.end()));
				
				if ( nameMatcher.lookingAt() ) {
					
					System.out.println(nameMatcher.group(0));
					
					person.setSurfaceText(nameMatcher.group(0));
					person.addService(service);
					list.add(person);
					
					person = new Person();
					service = new Service();
				}
			}

			// Check for service number and rank these if not already found.
			
			if (service.getNumber() == "") Parser.numberNew(current, service);
			if (service.getRank() == "UNK") Parser.rank(current, service);
			
			System.out.println(service);
			
		}

		for ( Person p: list ) {
			
			System.out.println(p);
			System.out.println(p.getService().size());	
			if (p.getService().size() > 0) System.out.println(p.getService().iterator().next());
			System.out.println(p.getSurfaceText());
		}
		
		System.out.println("found: " + list.size());
	}
	
	

}
