package soldiers.utilities;

import java.sql.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.search.CandidateScore;
import soldiers.search.PersonFinder;

public class Compare {

	// Are two soldiers the same or different?
	// If they're the same, which is the most complete record?
	// How should two entries for the same soldier be merged?
	
	private Person personA, personB;
	private PersonFinder finder;
	
	public Compare() {
		finder = new PersonFinder();
	}

	public Compare(Person personA, Person personB) {
		this();
		setSoldiers(personA, personB);
	}

	public void setSoldiers(Person personA, Person personB) {
		
		this.personA = personA;
		this.personB = personB;		
	}

	@Override
	public String toString() {

		StringBuffer buffer = new StringBuffer();
		buffer.append(personA.toString());
		buffer.append("\n");
		buffer.append(personB.toString());
		return buffer.toString();
	}
	
	public void makeComparison() {
		
		CandidateScore score = finder.scoreCandidate(personA, personB);
		//score.setNumber(0);

		System.out.println("overall score: " + score.getOverallScore());
		System.out.println("overall - number: " + (score.getOverallScore() - score.getNumber()));
		System.out.println(score);
		
		if ( score.getOverallScore() - score.getNumber() == 0 ) {
			
			System.out.println("different service numbers ...");
		}
		
		System.out.println("surname: " + score.getSurname());

		if ( score.getSurname() == 0 ) {
			
			System.out.println("same surname: " + personA.getSurname());
		}
		
		if ( score.getInitials() == 0 ) {
			
			System.out.println("same initials: " + personA.getInitials());
		}

		System.out.println(personA.getService());
		System.out.println(personB.getService());
		
		compareBirth();
		compareDeath();
		
		compareService();
	}
	
	private void compareBirth() {
		
		Date dateA   = personA.getBirth();      Date dateB   = personB.getBirth();
		Date afterA  = personA.getBornafter();  Date afterB  = personB.getBornafter();
		Date beforeA = personA.getBornbefore(); Date beforeB = personB.getBornbefore();
		
		System.out.println("compare birth ...");
		
		if ( dateA == null && dateB != null ) {
			
			System.out.println("update A birth date from B - set before and after to null");
		}
		else if ( dateA != null && dateB != null ) {
			
			System.out.println("birth date: " + dateA + " != " + dateB + " - which is correct?");
		}
		else if ( dateA != dateB  ) {
			
			System.out.println("birth date: " + dateA + " != " + dateB + " - ...");
		}
		else {
			System.out.println("neither has birth date");
		}
		
		if ( afterA == null && afterB != null ) {
			
			System.out.println("update A born after date from B");
		}
		else if ( afterA != null && afterB != null ) {
			
			System.out.println("birth after: " + afterA + " != " + afterB + " - which is correct?");
		}
		else {
			System.out.println("neither has birth after date");
		}
		
		if ( beforeA == null && beforeB != null ) {
			
			System.out.println("update A born before date from B");
		}
		else if ( beforeA != null && beforeA != null ) {
			
			System.out.println("birth before: " + beforeA + " != " + beforeA + " - which is correct?");
		}
		else {
			System.out.println("neither has birth before date");
		}
	}
	
	private void compareDeath() {
		
		System.out.println("compare death ...");
		Date dateA = personA.getDeath(); Date dateB = personB.getDeath();
		Date afterA  = personA.getDiedafter();  Date afterB  = personB.getDiedafter();
		Date beforeA = personA.getDiedbefore(); Date beforeB = personB.getDiedbefore();
		
		if ( dateA == null && dateB != null ) {
			
			System.out.println("update A death date from B - set before and after to null");
		}
		else if ( dateA != null && dateB != null ) {
			
			System.out.println("death date: " + dateA + " != " + dateB + " - which is correct?");
		}
		else if ( dateA != dateB  ) {
			
			System.out.println("death date: " + dateA + " != " + dateB + " - ...");
		}
		else {
			System.out.println("neither has death date");
		}
		
		if ( afterA == null && afterB != null ) {
			
			System.out.println("update A death after date from B");
		}
		else if ( afterA != null && afterB != null ) {
			
			System.out.println("death after: " + afterA + " != " + afterB + " - which is correct?");
		}
		else {
			System.out.println("neither has death after date");
		}
		
		if ( beforeA == null && beforeB != null ) {
			
			System.out.println("update A death before date from B");
		}
		else if ( beforeA != null && beforeA != null ) {
			
			System.out.println("death before: " + beforeA + " != " + beforeA + " - which is correct?");
		}
		else {
			System.out.println("neither has death before date");
		}
	}
	
	private void compareService() {
		
		Map<String, Set<Service>> serviceMapA = makeServiceNumberMap(personA);
		Map<String, Set<Service>> serviceMapB = makeServiceNumberMap(personB);
		
		if ( serviceMapA.keySet().containsAll(serviceMapB.keySet()) ) {
			System.out.println("SAME");
		}
		else {
			System.out.println("DIFFERENT");
			System.out.println(serviceMapA.keySet() + " != " + serviceMapB.keySet());
		}
	}
	
	public void makeComparison2() {
		
		
		compareService();
	}
	
	private Map<String, Set<Service>> makeServiceNumberMap(Person person) {
		
		HashMap<String, Set<Service>> map = new HashMap<String,Set<Service>>();
		
		for (Service service: person.getService() ) {
			System.out.println(service);
			String number = service.getNumber();
			Set<Service> serviceWithNumber = map.get(number);
			if ( serviceWithNumber == null )  serviceWithNumber = new HashSet<Service>();
			map.put(number, serviceWithNumber);
		}
		return map;
	}

}
