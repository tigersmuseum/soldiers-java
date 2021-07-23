package soldiers.database;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class Person {

	private String surname;
	private String forenames;
	private String initials;
	//private String rank;
	//private String number;
	private String suffix;
	private String title;
	private String surfaceText;
	private Date birth, death;
	private Date bornafter, bornbefore, diedafter, diedbefore;
	private Set<Service> service = new HashSet<Service>();

	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
	
	static Pattern rankPattern = Pattern.compile("(A/)?(Pte|Drumr|Dmr|Dvr|Cpl|L/Cpl|LCpl|C/Sgt|C Sgt|CR:SGT|L/Sgt|Sergt|Sjt|QMSgt|Qr Mr Sjt|Sgt Major|Sgt|CQMS|CSM|WO2|W O Cl2|WO Cl II|WO1|2Lt|Lt & Adjt|Lt Col|Lt|Lieut|Captain|Capt|T/Capt|Major|Maj|Colonel|Col|Brigadier General|Brigadier-General|General|Gen)\\b(\\(Temp\\)\\b)?");
	static Pattern numberPattern = Pattern.compile("(No\\.?\\s+)?\\d[\\d-/]+\\b");
	static Pattern initialsPattern = Pattern.compile("^(([A-Z]\\s)+).+");
	static Pattern suffixPattern = Pattern.compile("(\\s+(GCMG|KCB|DSO|MC|VC|RAMC|RE|MM|ASC))+$");
	static Pattern delimPattern = Pattern.compile("\\w{4}(\\.|,)");
	static Pattern regt = Pattern.compile("\\d+(/\\d+)?(th|st|nd|rd)(\\s+[^\\s]+){1,6}?\\s+(Regt|Rgt|Brigade|Watch|Rifles|Yorks|Lancs|Hampshires|Cambs|RSR|Yeomanry|Hants|Sussex|Sx|Bde|RWF|Notts|Derby|Fusilier|Forester|Territorial|Warwicks|Fus|Herts|Borderer|KRR|DLI|LI|RWF|Company)s?");


	
	private long soldierId = -1;

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		
		if ( surname == null ) return;
		String normal = surname.replaceAll("’", "'");
		this.surname = normal.toUpperCase();
	}

	public String getForenames() {
		return forenames;
	}

	public void setForenames(String forenames) {
		if ( forenames != null ) this.forenames = forenames.toUpperCase();
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(String initials) {
		
		if ( initials != null ) this.initials = normalizeInitials(initials);
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getSoldierId() {
		return soldierId;
	}

	public void setSoldierId(long soldierId) {
		this.soldierId = soldierId;
	}

	public Date getBirth() {
		return birth;
	}

	public void setBirth(Date birth) {
		this.birth = birth;
	}

	public Date getDeath() {
		return death;
	}

	public void setDeath(Date death) {
		this.death = death;
	}

	public Date getBornafter() {
		return bornafter;
	}

	public void setBornafter(Date bornafter) {
		this.bornafter = bornafter;
	}

	public Date getBornbefore() {
		return bornbefore;
	}

	public void setBornbefore(Date bornbefore) {
		this.bornbefore = bornbefore;
	}

	public Date getDiedafter() {
		return diedafter;
	}

	public void setDiedafter(Date diedafter) {
		this.diedafter = diedafter;
	}

	public Date getDiedbefore() {
		return diedbefore;
	}

	public void setDiedbefore(Date diedbefore) {
		this.diedbefore = diedbefore;
	}

	public Set<Service> getService() {
		return service;
	}

	public void setService(Set<Service> service) {
		this.service = service;
	}
	
	public void addService(Service svc) {
		
		service.add(svc);
	}

	@Override
	public String toString() {

		String person = String.format("%d: surname=%s, initials=%s, forenames=%s, suffix=%s", soldierId, surname, initials, forenames, suffix);
		return person;
	}
	
	public void serializePerson(ContentHandler ch) throws SAXException {
		
		ch.startPrefixMapping("", SoldiersModel.XML_NAMESPACE);

		AttributesImpl attr = new AttributesImpl();
		if ( soldierId > 0 ) attr.addAttribute("", "sid",  "sid", "Integer", String.valueOf(soldierId));

		ch.startElement(SoldiersModel.XML_NAMESPACE, "person", "person", attr);

		ch.startElement(SoldiersModel.XML_NAMESPACE, "surname", "surname", new AttributesImpl());
		if ( surname != null ) ch.characters(surname.toCharArray(), 0, surname.length());	
		ch.endElement(SoldiersModel.XML_NAMESPACE, "surname", "surname");

		ch.startElement(SoldiersModel.XML_NAMESPACE, "initials", "initials", new AttributesImpl());
		if ( initials != null ) ch.characters(initials.toCharArray(), 0, initials.length());	
		ch.endElement(SoldiersModel.XML_NAMESPACE, "initials", "initials");

		ch.startElement(SoldiersModel.XML_NAMESPACE, "forenames", "forenames", new AttributesImpl());
		if ( forenames != null ) ch.characters(forenames.toCharArray(), 0, forenames.length());	
		ch.endElement(SoldiersModel.XML_NAMESPACE, "forenames", "forenames");
		
		attr = new AttributesImpl();
		if (birth != null)  attr.addAttribute("", "date",  "date", "String",  formatter.format(birth));
		if (bornafter != null)  attr.addAttribute("", "after",  "after", "String",  formatter.format(bornafter));
		if (bornbefore != null)  attr.addAttribute("", "before",  "before", "String",  formatter.format(bornbefore));

		if ( attr.getLength() > 0 ) {
			ch.startElement(SoldiersModel.XML_NAMESPACE, "birth", "birth", attr);
			ch.endElement(SoldiersModel.XML_NAMESPACE, "birth", "birth");
		}
		
		
		attr = new AttributesImpl();
		if (death != null)  attr.addAttribute("", "date",  "date", "String",  formatter.format(death));

		if ( attr.getLength() > 0 ) {
			ch.startElement(SoldiersModel.XML_NAMESPACE, "death", "death", attr);
			ch.endElement(SoldiersModel.XML_NAMESPACE, "death", "death");
		}
		
		if ( surfaceText != null ) {
			
			ch.startElement(SoldiersModel.XML_NAMESPACE, "text", "text", new AttributesImpl());
			ch.characters(surfaceText.toCharArray(), 0, surfaceText.length());
			ch.endElement(SoldiersModel.XML_NAMESPACE, "text", "text");
		}
		
		Set<Service> service = getService();
					
		if ( service.size() > 0 ) {
			
			ch.startElement(SoldiersModel.XML_NAMESPACE, "service", "service", new AttributesImpl());		
			for ( Service svc: service)  svc.serializeService(ch);
			ch.endElement(SoldiersModel.XML_NAMESPACE, "service", "service");
		}
				
		ch.endElement(SoldiersModel.XML_NAMESPACE, "person", "person");	
	}

    private String normalizeInitials(String initials) {
    	
		String addspaces = initials.toUpperCase().replaceAll("([A-Z])", "$1 ");
		String normalizespaces = addspaces.replaceAll("\\s+", " ").trim();
		return normalizespaces;
    }
    
    public String getContent() {
    
    	StringBuffer content = new StringBuffer();
    	
    	Set<Service> service = getService();
    	
    	if  (! service.isEmpty() ) {
    		
    		Service svc = service.iterator().next();
        	if ( svc.getNumber() != null ) content.append(svc.getNumber() + " ");
        	if ( svc.getRank() != null ) content.append(svc.getRank() + " ");
    	}
    	
    	if ( initials != null ) content.append(initials + " ");
    	if ( surname != null ) content.append(surname);
    	if ( suffix != null ) content.append(" " + suffix);
    	
    	return content.toString().trim();
    }
    
    public String getSort() {
    
    	StringBuffer sort = new StringBuffer();
    	
    	sort.append(surname);
    	if ( initials != null ) sort.append(", " + initials);
    	
    	return sort.toString().trim();
    }

	@Override
	public boolean equals(Object obj) {

		Person other = (Person) obj;
		if (soldierId > 0 && other.getSoldierId() == soldierId) return true;
		else return false;
	}

	@Override
	public int hashCode() {
		
		if (soldierId > 0) return Long.valueOf(soldierId).hashCode();
		else return super.hashCode();
	}

	
	public String getSurfaceText() {
		return surfaceText;
	}

	public void setSurfaceText(String surfaceText) {
		this.surfaceText = surfaceText.replaceAll("\\s+", " ").trim();
	}

	public static Person parseText(String surface) {
		
		Person person = new Person();
		person.setSurfaceText(surface.replaceAll("\\.", " ").replaceAll("\\s+", " "));
		
		String text = person.getSurfaceText();
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
			else if ( name.matches("^([A-Z][a-z]+\\s+)+[A-Z]+$") ) {
				
				// full forenames, assume surname is last
				person.setSurname(names[names.length - 1]);
			}

			person.addService(svc);
		}
		
		return person;
	}
		
}
