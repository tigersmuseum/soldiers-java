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
		String surface ="4400\r\n" + 
				"7364 Pte   Hunter      S.   Granted        45           6.12.06\r\n" + 
				"6364 Dr     Smith       F.   Granted        75           3.12.06\r\n" + 
				"6246 L.c.   Brown      F.                                 \r\n" + 
				"6410 Pte   Ierice       W.   Granted       75            6. 1.07\r\n" + 
				"6936 Pte   Tower      R.    Granted       65            6.11.07\r\n" + 
				"6440 Pte   Taylor      W.   Granted       75           29. 1.07\r\n" + 
				"7136 Pte   Mears      T.    Restored     45           17.12.06 \r\n" + 
				"";
		
		String text = surface.replaceAll("\\p{javaSpaceChar}", " ").trim();
		//String text = surface;
				
		Matcher spaceMatcher = whitespace.matcher(text);
		
		Person person = new Person();
		Service service = new Service();
		
		// Step through the text looking for names. There might be an number of soldiers mentioned, so we create a list
		
		List<Person> list = new ArrayList<Person>();
		
/*		while ( spaceMatcher.find() ) {
			
			String current = text.substring(spaceMatcher.end());
			//System.out.println(current);
			
			// If we have a rank, we expect it to be followed by a name. Lots of ways this might be expressed, so start
			// by trying to find a run of text that captures any potential initials, forenames and surnames.
			
			if ( service.getRank() != "UNK" && person.getSurfaceText() == null ) {
				
				Matcher nameMatcher = Parser.namePattern.matcher(current);
				
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
			
			if (service.getNumber() == "")   Parser.numberNew(current, service);
			if (service.getRank() == "UNK")  Parser.rank(current, service);
			
			System.out.println(service);
			
		}

		for ( Person p: list ) {
			
			System.out.println(p);
			System.out.println(p.getService().size());	
			if (p.getService().size() > 0) System.out.println(p.getService().iterator().next());
			System.out.println(p.getSurfaceText());
			Parser.name(trimUnit(p.getSurfaceText()), p);
		}
		
		System.out.println("found: " + list.size());

		for ( Person p: list ) {
			
			System.out.println(p.getContent());
			//System.out.println(p);
		}
*/		
		findMention(text);
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
	
	
	public static void findMentionX(String text) {
		
		Service service = new Service();

		int offset = 0;
		
		while ( offset < text.length() ) {
			
			System.out.println(">>" + offset);
			
			Matcher spaceMatcher = whitespace.matcher(text.substring(offset));
			System.out.println("]]" + text.substring(offset));
			
			int lastspace = 0;
			
			while ( spaceMatcher.find() ) {
				
				String current = text.substring(spaceMatcher.end());
				lastspace = spaceMatcher.end();
				
				if (service.getNumber() == "")   Parser.numberNew(current, service);
				Parser.rank(current, service);
				
				if ( service.getRank() != "UNK" ) {
					
					System.out.println(service.getRank() + ", " + spaceMatcher.end() + " =" + text.substring(spaceMatcher.end() + service.getRank().length()).trim());
					String remaining = text.substring(spaceMatcher.end() + service.getRank().length()).trim();
					
					Matcher nameMatcher = Parser.namePattern.matcher(remaining);
					
					if ( nameMatcher.lookingAt() ) {
						
						System.out.println(nameMatcher.group(0));
						
						Person person = new Person();
						person.setSurfaceText(nameMatcher.group(0));
						person.addService(service);
						Parser.name(nameMatcher.group(0), person);
						
						service = new Service();
						
						System.out.println(person.getContent());
					}

					break;
				}
			}
			
			offset += lastspace;

		}
		
	}
	
	
	public static void findMention(String text) {
		
		List<Person> list = new ArrayList<Person>();

		String remaining = text;
		
		while ( remaining.length() > 0 ) {
			
			System.out.println(">>" + remaining);
			
			Matcher spaceMatcher = whitespace.matcher(remaining);
						
			Service service = new Service();
			StringBuffer surface = new StringBuffer();

			while ( service.getRank() == "UNK" && spaceMatcher.find() ) {
				
				String current = remaining.substring(spaceMatcher.end());
			//	System.out.println("-- " + current);
				if (service.getNumber() == "")   surface.append(Parser.numberNew(current, service));
				surface.append(' ');
				surface.append(Parser.rank(current, service));				
				surface.append(' ');
			//	System.out.println("++ " + current);
			}
			
			if ( service.getRank() != "UNK" ) {
				
				String nxt = remaining.substring(spaceMatcher.end() + service.getRank().length()).trim();
				
				Matcher nameMatcher = Parser.namePattern.matcher(nxt);
				
				if ( nameMatcher.lookingAt() ) {
					
					Person person = new Person();
					String name = trimUnit(nameMatcher.group(0));
					surface.append(name);
					person.addService(service);
					Parser.name(name, person);
					person.setSurfaceText(surface.toString().replaceAll("//s+", " ").trim());
					
					System.out.println("** " + person.getSurfaceText());
					System.out.println("** " + person.getContent());
					
					list.add(person);

					remaining = nxt.substring(nameMatcher.end()).trim();
				}
				else {
					remaining = nxt.substring(service.getRank().length());
					service = new Service();
				}
			}
			else {
				remaining = "";
			}

		}
		
		for ( Person p: list ) {
			
			System.out.println(p.getContent());
		}
		
	}
}
