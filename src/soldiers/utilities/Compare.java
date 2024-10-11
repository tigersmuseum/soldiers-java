package soldiers.utilities;

import java.sql.Date;

import soldiers.database.Person;
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
			
			System.out.println("update A birth date from B - set before and after to null ");
		}
		else if ( dateA != null && dateB != null ) {
			
			System.out.println("date: " + dateA + " != " + dateB + " - which is correct?");
		}
		else if ( dateA != dateB  ) {
			
			System.out.println("date: " + dateA + " != " + dateB + " - ...");
		}
		
		if ( afterA == null && afterB != null ) {
			
			System.out.println("update A born after date from B");
		}
		else if ( afterA != null && afterB != null ) {
			
			System.out.println("after: " + afterA + " != " + afterB + " - which is correct?");
		}
		
		if ( beforeA == null && beforeB != null ) {
			
			System.out.println("update A born before date from B");
		}
		else if ( beforeA != null && beforeA != null ) {
			
			System.out.println("before: " + beforeA + " != " + beforeA + " - which is correct?");
		}
	}
	
	private void compareDeath() {
		
		Date a = personA.getDeath(); Date b = personB.getDeath();
		
		if (a == null && b == null ) System.out.println("both null");
	}
}
