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
		System.out.println(score.getOverallScore());
		
		System.out.println(score.getOverallScore());
		System.out.println(score.getOverallScore() - score.getNumber());
		
		if ( score.getOverallScore() - score.getNumber() == 0 ) {
			
			System.out.println("different service numbers ...");
		}
		
		System.out.println(score.getSurname());

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
	}
	
	private void compareBirth() {
		
		Date dateA   = personA.getBirth();      Date dateB   = personB.getBirth();
		Date afterA  = personA.getBornafter();  Date afterB  = personB.getBornafter();
		Date beforeA = personA.getBornbefore(); Date beforeB = personB.getBornbefore();
		
		if ( dateA == null && dateB != null ) {
			
			System.out.println("update A birth date from B - set before and after to null");
		}
		else if ( dateA != null && dateB != null ) {
			
			System.out.println("birth date: " + dateA + " != " + dateB + " - which is correct?");
		}
		else if ( dateA != dateB  ) {
			
			System.out.println("birth date: " + dateA + " != " + dateB + " - ...");
		}
		
		if ( afterA == null && afterB != null ) {
			
			System.out.println("update A born after date from B");
		}
		else if ( afterA != null && afterB != null ) {
			
			System.out.println("birth after: " + afterA + " != " + afterB + " - which is correct?");
		}
		
		if ( beforeA == null && beforeB != null ) {
			
			System.out.println("update A born before date from B");
		}
		else if ( beforeA != null && beforeA != null ) {
			
			System.out.println("birth before: " + beforeA + " != " + beforeA + " - which is correct?");
		}
	}
	
	private void compareDeath() {
		
		Date a = personA.getDeath(); Date b = personB.getDeath();
		
		if (a == null && b == null ) System.out.println("both null");
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
