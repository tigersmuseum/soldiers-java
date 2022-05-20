package soldiers.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soldiers.database.Person;
import soldiers.database.Service;

public class Parser {

	static Pattern rankPattern = Pattern.compile("(A/)?(Private|Pte|Drumr|Drummer|Dmr|Dvr|GNR|Gnr|Spr|Cpl|L/Cpl|LCPL|L Cpl|LCpl|LCorpl|Sgt Drummer|C/Sgt|CSjt|C/Sjt|C Sgt|CRSGT|CR SGT|L/Sgt|Lance Sergeant|L/Sjt|Sergt|Sjt|QMSgt|QMS|Qr Mr Sjt|Sergeant|Sgt Major|S/ Mjr|Sgt Maj|S Mjr|SMjr|Sgt|Band Sjt|CQMS|CSM|CSMjr|RSM|RQMS|WO2|W O Cl2|WO Cl II|WO Cl2|WO1|2Lt|2/Lt |2Lieut|2 Lieut|Lt & Adjt|Lt Col|Lieutenant|Lt|Lieut|Captain|Capt|T/Capt|Major|Maj|Colonel|Col|Brigadier|Brig|Brig Gen|Brigadier General|Brigadier-General|General|Gen|Surgeon)\\b(\\(Temp\\)\\b)?\\.?");
	static Pattern numberPattern = Pattern.compile("(No\\.?\\s+)?([A-Z]{1,2}/)?\\d[\\d-/]+(\\s)");
	static Pattern initialsPattern = Pattern.compile("^(([A-Z](\\s|\\.\\s?))+).+");
	//public static Pattern namePattern = Pattern.compile("([A-Z][A-Za-z]*(\\-|\\.\\s*|\\s{1,2}|$))+");
	public static Pattern namePattern = Pattern.compile("(([A-Z](\\s|\\.\\s?))+)?([A-Z][a-z]+(\\-|\\s{1,2}|\\.|$))+(\\s+([A-Z](\\s|\\.\\s?))+)?");
	static Pattern suffixPattern = Pattern.compile("(\\s+(GCMG|KSCG|KCB|DSO|MC|VC|RAMC|DCM|OBE|CBE|RE|MM|CB|CME|TD|ASC))+$");
	static Pattern delimPattern = Pattern.compile("\\w{4}(\\.|,)");
	static Pattern regt = Pattern.compile("\\d+(/\\d+)?(th|st|nd|rd)(\\s+[^\\s]+){1,6}?\\s+(Regt|Rgt|Brigade|Watch|Rifles|Yorks|Lancs|Hampshires|Cambs|RSR|Yeomanry|Hants|Sussex|Sx|Bde|RWF|Notts|Derby|Fusilier|Forester|Territorial|Warwicks|Fus|Herts|Borderer|KRR|DLI|LI|RWF|Company)s?");

	public static Person parsePersonMention(String surface) {
		
		Person person = new Person();
		person.setSurfaceText(surface);
		person.setForceToUpper(false);
		
		Service service = new Service();

		String text = surface.replaceAll("\\p{javaSpaceChar}", " ").trim();
		//text = text.replaceAll("([A-Z])\\.", "$1");
		text = text.replaceAll("\\.", "");
		
		System.out.println("### " + text);

		text = suffix(text, person);
		text = numberFind(text, service);
		text = rankOld(text,service);

		String name = text;

		name(name, person);
		person.addService(service);

		return person;
	}
	
	
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
	
	
	public static String rankOld(String text, Service service) {
		
		String retval = text;
		
		Matcher rankMatcher = rankPattern.matcher(text);
		
		if ( rankMatcher.find() ) {
			
			String rank = rankMatcher.group(0).trim();
			service.setRank(rank);
			retval = text.replaceAll(rank, "").trim();
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
		
}
