package soldiers.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soldiers.database.Person;
import soldiers.database.Service;

public class Parser {

	static Pattern rankPattern = Pattern.compile("(A/)?(Pte|Drumr|Dmr|Dvr|Cpl|L/Cpl|LCpl|C/Sgt|C Sgt|CR:SGT|L/Sgt|Sergt|Sjt|QMSgt|Qr Mr Sjt|Sgt Major|Sgt|CQMS|CSM|WO2|W O Cl2|WO Cl II|WO1|2Lt|Lt & Adjt|Lt Col|Lt|Lieut|Captain|Capt|T/Capt|Major|Maj|Colonel|Col|Brigadier General|Brigadier-General|General|Gen)\\b(\\(Temp\\)\\b)?");
	static Pattern numberPattern = Pattern.compile("(No\\.?\\s+)?\\d[\\d-/]+\\b");
	static Pattern initialsPattern = Pattern.compile("^(([A-Z]\\s)+).+");
	static Pattern suffixPattern = Pattern.compile("(\\s+(GCMG|KCB|DSO|MC|VC|RAMC|RE|MM|ASC))+$");
	static Pattern delimPattern = Pattern.compile("\\w{4}(\\.|,)");
	static Pattern regt = Pattern.compile("\\d+(/\\d+)?(th|st|nd|rd)(\\s+[^\\s]+){1,6}?\\s+(Regt|Rgt|Brigade|Watch|Rifles|Yorks|Lancs|Hampshires|Cambs|RSR|Yeomanry|Hants|Sussex|Sx|Bde|RWF|Notts|Derby|Fusilier|Forester|Territorial|Warwicks|Fus|Herts|Borderer|KRR|DLI|LI|RWF|Company)s?");

	public static Person parsePersonMentionX(String surface) {
		
		Person person = new Person();
		person.setSurfaceText(surface);
		
		String temp = surface.replaceAll("(\\d/)?Hamps|\\d\\dth Regt", "");
		temp = temp.replaceAll("\\d{4}$", "");
		//System.out.println(temp);
		
		String text = temp.replaceAll("\\s+", " ").replaceAll("\\.", " ");
		String name;
		
		Matcher rankMatcher = rankPattern.matcher(text);
		Matcher numberMatcher = numberPattern.matcher(text);
		Matcher suffixMatcher = suffixPattern.matcher(text);
		
		if ( rankMatcher.find() ) {
			
			Service svc = new Service();
			
			String rnk = rankMatcher.group(0).trim();
			svc.setRank(rnk);
			name = text.replaceAll(rnk, "").replaceAll("\\s+", " ").trim();
			
			if ( numberMatcher.find() ) {
				
				String ntxt = numberMatcher.group(0).trim();
				name = name.replaceAll(ntxt, "").replaceAll("\\s+", " ").trim();
				svc.setNumber(ntxt.replaceAll("-", "/").replaceAll("No\\.?\\s+?", "").trim());
			}

			if ( suffixMatcher.find() ) {
				
				String stxt = suffixMatcher.group(0).trim();
				name = name.replaceAll(stxt, "").replaceAll("\\s+", " ").trim();
				person.setSuffix(stxt);
			}
			
			Matcher initialsMatcher = initialsPattern.matcher(name);

			if ( initialsMatcher.matches() ) {
				
				// initials spaced out...
				String initials = initialsMatcher.group(1).trim();
				person.setInitials(initials);
				name = name.replaceFirst(initials, "").trim();
			}
			
			name = name.replaceAll("\\p{javaSpaceChar}", " ").trim() + "]";
			String[] names = name.split("[\\s,]+");
			
			if ( names.length == 1 ) {
				
				// surname only
				person.setSurname(names[0]);
			}
			else if (names.length == 2 && names[0].length() > names[1].length() ) {
				
				// surname followed by initials
				person.setSurname(names[0]);
				if ( names[1].matches("^[A-Z]+$") ) person.setInitials(names[1].toUpperCase().replaceAll("([A-Z])", "$1 ").trim());
			}
			else if (names.length == 2 && names[1].length() > names[0].length() ) {
				
				// initials followed by surname			
				person.setSurname(names[1]);
				if ( names[0].matches("^[A-Z]+$") ) person.setInitials(names[0].toUpperCase().replaceAll("([A-Z])", "$1 ").trim());
			}
			else if ( name.matches("^([A-Z][a-z]+\\s+)+[A-Z]\\w+$") ) {
				
				// full forenames, assume surname is last
				String surname = names[names.length - 1];
				person.setSurname(surname);
				
				String forenames = name.replaceAll(name, surname).trim();
				person.setForenames(forenames);
			}

			person.addService(svc);
		}
		
		return person;
	}
		
	public static Person parsePersonMention(String surface) {
		
		Person person = new Person();
		person.setSurfaceText(surface);
		
		String temp = surface.replaceAll("([A-Z])\\.", "$1").split(",|\\.")[0];
		System.out.println(surface);
		System.out.println(temp);
		return person;
	}
		
}
