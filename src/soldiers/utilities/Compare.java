package soldiers.utilities;

import java.sql.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.search.CandidateScore;
import soldiers.search.Scorer;

public class Compare {

	// Are two soldiers the same or different?
	// If they're the same, which is the most complete record?
	// How should two entries for the same soldier be merged?
	// Assume personA is authoritative and compare personB with personA
	
	private Person personA, personB;
	
	public Compare() {
	}

	public Compare(Person personA, Person personB) {
		this();
		setSoldiers(personA, personB);
	}

	public void setSoldiers(Person personA, Person personB) {
		
		this.personA = personA;
		this.personB = personB;		
	}
	
	public Person makeComparison() {

		Person diffs = new Person();

		Scorer scorer = new Scorer();
		CandidateScore score = scorer.scoreCandidate(personA, personB);
		System.out.println("score: " + score);
		
		if ( score.getInitials() != 0 ) diffs.setInitials(personB.getInitials());
		if ( score.getForenames() != 0 ) diffs.setForenames(personB.getForenames());
		if ( score.getSurname() != 0 )   diffs.setSurname(personB.getSurname());
			
		compareDates(diffs);
		compareService(diffs);
		
		return diffs;
	}
	
	private void compareDates(Person diffs) {
		
		diffs.setBirth(compare(personA.getBirth(), personB.getBirth()));
		diffs.setBornafter(compare(personA.getBornafter(), personB.getBornafter()));
		diffs.setBornbefore(compare(personA.getBornbefore(), personB.getBornbefore()));
		
		diffs.setDeath(compare(personA.getDeath(), personB.getDeath()));
		diffs.setDiedafter(compare(personA.getDiedafter(), personB.getDiedafter()));
		diffs.setDiedbefore(compare(personA.getDiedbefore(), personB.getDiedbefore()));
	}
	
	private Date compare(Date dateA, Date dateB ) {

		return ( dateB != null && !dateB.equals(dateA) ) ? dateB : null;
	}
	
	private void compareService(Person diffs) {
		
		Map<String, Set<Service>> serviceMapA = makeServiceNumberMap(personA);
		Map<String, Set<Service>> serviceMapB = makeServiceNumberMap(personB);
		
		modifyServiceNumberMap(serviceMapA);
		
		if ( serviceMapA.keySet().containsAll(serviceMapB.keySet()) ) {
			System.out.println("SAME");
		}
		else {
			System.out.println("DIFFERENT");
			diffs.setService(personB.getService());
		}
	}
	
	private Map<String, Set<Service>> makeServiceNumberMap(Person person) {
		
		HashMap<String, Set<Service>> map = new HashMap<String,Set<Service>>();
		
		for (Service service: person.getService() ) {
			
			String number = service.getNumber();
			Set<Service> serviceWithNumber = map.get(number);
			if ( serviceWithNumber == null )  serviceWithNumber = new HashSet<Service>();
			serviceWithNumber.add(service);
			map.put(number, serviceWithNumber);
		}
		
		return map;
	}

	private void modifyServiceNumberMap(Map<String, Set<Service>> map) {
		
		// deal with entries where the number id of the form #/####
		
		for ( String number: map.keySet() ) {
			
			if ( number.contains("/")) {
				
				Set<Service> serviceWithNumber = map.get(number);
				map.put(number.substring(number.indexOf('/')+1), serviceWithNumber);
			}		
		}
	}
}
