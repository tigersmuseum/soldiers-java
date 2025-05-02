package soldiers.database;

import java.text.SimpleDateFormat;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import soldiers.utilities.ServiceComparator;

public class Person {

	private String surname;
	private String forenames;
	private String initials;
	private String suffix;
	private String title;
	private String surfaceText;
	private Date birth, death;
	private Date bornafter, bornbefore, diedafter, diedbefore;
	private Set<Service> service = new HashSet<Service>();
	private boolean forceToUpper = true;

	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	private ServiceComparator serviceComparator = new ServiceComparator();
	
	private long soldierId = -1;

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		
		if ( surname == null ) return;
		String normal = surname.replaceAll("�", "'");
		if ( forceToUpper )  this.surname = normal.toUpperCase();
		else  this.surname = normal;
	}

	public String getForenames() {
		return forenames;
	}

	public void setForenames(String forenames) {
		
		if ( forenames == null || forenames.replaceAll("\\s+", "").length() == 0 ) return;
		
		if ( forceToUpper )  this.forenames = forenames.toUpperCase();
		else  this.forenames = forenames;
		
		if ( getInitials() == null && this.forenames != null ) {
			
			String[] tokens = this.forenames.trim().split("\\s+");
			StringBuffer initials = new StringBuffer();
			
			for ( String token: tokens ) {
				
				initials.append(token.substring(0, 1));
			}
			
			this.setInitials(normalizeInitials(initials.toString()));
		}
	}

	void setForenamesOnly(String forenames) {
		
		if ( forceToUpper && forenames != null )  this.forenames = forenames.toUpperCase();
		else  this.forenames = forenames;
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(String initials) {
		
		if ( initials != null ) this.initials = normalizeInitials(initials);
	}

	void setInitialsAsGiven(String initials) {
		
		this.initials = initials;
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
		for (Service s: service)  s.setSoldierId(soldierId);
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
	
	public void setForceToUpper(boolean value) {		
		this.forceToUpper = value;
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
		if (diedafter != null)  attr.addAttribute("", "after",  "after", "String",  formatter.format(diedafter));
		if (diedbefore != null)  attr.addAttribute("", "before",  "before", "String",  formatter.format(diedbefore));

		if ( attr.getLength() > 0 ) {
			ch.startElement(SoldiersModel.XML_NAMESPACE, "death", "death", attr);
			ch.endElement(SoldiersModel.XML_NAMESPACE, "death", "death");
		}
		
		if ( surfaceText != null ) {
			
			ch.startElement(SoldiersModel.XML_NAMESPACE, "text", "text", new AttributesImpl());
			ch.characters(surfaceText.toCharArray(), 0, surfaceText.length());
			ch.endElement(SoldiersModel.XML_NAMESPACE, "text", "text");
		}
		
		//Set<Service> service = getService();
		List<Service> service = new ArrayList<Service>(getService());
					
		if ( service.size() > 0 ) {
			
			Collections.sort(service, serviceComparator);
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

	public boolean hasNumber(String number) {
		
		if ( number == null )  return false;
		
		Set<Service> service = getService();
		
		for ( Service record: service ) {
			
			if ( record.getNumber().equals(number) ) return true;
		}
		
		return false;
	}
}
