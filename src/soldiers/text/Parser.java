package soldiers.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soldiers.database.Person;
import soldiers.database.Service;

public class Parser {

	static Pattern whitespace = Pattern.compile("^|\\s+"); // match the start of the text, or any sequence of whitespace
	static Pattern rankPattern = Pattern.compile("(A/)?(Private|Pte|Drumr|Drummer|Dmr|Dvr|GNR|Gnr|Spr|Cpl|L/Cpl|L/C|LCPL|L Cpl|LCpl|LCorpl|Sgt Drummer|C/Sgt|CSjt|C/Sjt|C Sgt|CRSGT|CR SGT|L/Sgt|Lance Sergeant|L/Sjt|Sergt|Sjt|QMSgt|QMS|Qr Mr Sjt|Sergeant|Sgt Major|S/ Mjr|Sgt Maj|S Mjr|SMjr|Sgt|Band Sjt|CQMS|CSM|CSMjr|RSM|RQMS|WO2|W O Cl2|WO Cl II|WO Cl2|WO1|2Lt|2/Lt |2Lieut|2 Lieut|Lt & Adjt|Lt Col|Lieutenant|Lt|Lieut|Captain|Capt|T/Capt|Major|Maj|Colonel|Col|Brigadier|Brig|Brig Gen|Brigadier General|Brigadier-General|General|Gen|Surgeon)\\b(\\(Temp\\)\\b)?\\.?");
	static Pattern numberPattern = Pattern.compile("(No\\.?\\s+)?([A-Z]{1,2}/)?\\d[\\d-/]+(\\s)");
	static Pattern initialsPattern = Pattern.compile("^(([A-Z](\\s|\\.\\s?))+).+");

	public static Pattern namePattern = Pattern.compile("(([A-Z](\\s|\\.\\s?))+)?([A-Z][a-z]+(\\-|\\s{1,2}|\\.|$))+(\\s+([A-Z](\\s|\\.\\s?))+)?");
	static Pattern suffixPattern = Pattern.compile("(\\s+(GCMG|KSCG|KCB|DSO|MC|VC|RAMC|DCM|OBE|CBE|RE|MM|CB|CME|TD|ASC))+$");
	public static Pattern companyPattern = Pattern.compile("[A-Z]\\s+Coy");
	
	public static String suffix(String text, Person person) {
		
		String retval = text;
		
		Matcher suffixMatcher = suffixPattern.matcher(text);
		
		if ( suffixMatcher.find() ) {
			
			String suffix = suffixMatcher.group(0).trim();
			person.setSuffix(suffix);
			retval = text.replaceAll(suffix, "").trim();
		}
		
		return retval;
	}
	
	
	public static String rank(String text, Service service) {
		
		String retval = "";
		
		Matcher rankMatcher = rankPattern.matcher(text);
		
		if ( rankMatcher.lookingAt() ) {
			
			String rank = rankMatcher.group(0).trim();
			service.setRank(rank);
			retval = text.substring(rankMatcher.start(), rankMatcher.end());
		}
		
		return retval;
	}
	
	
	public static String numberFind(String text, Service service) {
		
		String retval = text;
		
		Matcher numberMatcher = numberPattern.matcher(text);
		
		if ( numberMatcher.find() ) {
			
			String number = numberMatcher.group(0).trim();
			service.setNumber(number.replaceAll("-", "/").replaceAll("No\\.?\\s+?", "").trim());
			retval = text.replaceAll(number, "").trim();
		}
		
		return retval;
	}
	
	
	public static void numberLookAt(String text, Service service) {
		
		Matcher numberMatcher = numberPattern.matcher(text);
		
		if ( numberMatcher.lookingAt() ) {
			
			String number = numberMatcher.group(0).trim();
			service.setNumber(number.replaceAll("-", "/").replaceAll("No\\.?\\s+?", "").trim());
		}
	}
	
	
	public static String name(String name, Person person) {
		
		String retval = name;
		name = name.replaceAll("\\.", " ").replaceAll("//s+", " ");
		
		Matcher initialsMatcher = initialsPattern.matcher(name);

		if ( initialsMatcher.matches() ) {
			
			// initials spaced out...
			String initials = initialsMatcher.group(1).trim();
			person.setInitials(initials);
			name = name.replaceFirst(initials, "").trim();
		}
		
		String[] names = name.split("[\\s,]+");
		
		if ( names.length == 1 ) {
			
			// surname only
			person.setSurname(names[0]);
		}
		else if (names.length == 2 && names[0].length() > names[1].length() && names[1].matches("^[A-Z]+$") ) {
			
			// surname followed by initials
			person.setSurname(names[0]);
			person.setInitials(names[1].toUpperCase().replaceAll("([A-Z])", "$1 ").trim());
		}
		else if (names.length == 2 && names[1].length() > names[0].length() && names[0].matches("^[A-Z]+$") ) {
			
			// initials followed by surname			
			person.setSurname(names[1]);
			person.setInitials(names[0].toUpperCase().replaceAll("([A-Z])", "$1 ").trim());
		}
		else if ( name.matches("^([A-Z]([a-z]+)?\\s+)+[A-Z]\\w+$") ) {
			
			String surname = names[names.length - 1];
			person.setSurname(surname);
			
			String forenames = name.replaceAll(surname, "").trim();
			person.setForenames(forenames);
		}
		else {	// default (same as above)	
			String surname = names[names.length - 1];
			person.setSurname(surname);
			
			String forenames = name.replaceAll(surname, "").trim();
			person.setForenames(forenames);		
		}
		
		return retval;
	}

	public static String trimUnit(String text) {
		
		String retval = text;
		
		Matcher companyMatcher = Parser.companyPattern.matcher(text);

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
			
			// Look for a rank, but expect to possibly find service number on the way.
			
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
