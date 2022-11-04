package soldiers.test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soldiers.database.Normalize;
import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.text.Parser;

public class ParserTest {
	
	static Pattern whitespace = Pattern.compile("^|\\s+"); // match the start of the text, or any sequence of whitespace
	public static Pattern companyPattern = Pattern.compile("[A-Z]\\s+Coy"); // match a unit (signals the end of a name string)

	public static void main(String[] args) {

		String surface = "3117 Pte T Jarvis";
		
		String text = surface.replaceAll("\\p{javaSpaceChar}", " ").trim();
				
		List<Person> list = findMention(text);
		System.out.println(list.size());
		Normalize.normalizeRank(list);
		
		for ( Person p: list ) {
			
			System.out.printf("(%d) %s = %s", p.getSoldierId(), p.getContent(), p.getSurfaceText());
		}
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
