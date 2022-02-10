package soldiers.search;

import soldiers.database.Person;

public class Candidate {
	
	private String identifier = "";

	private Person person;
	private CandidateScore score;

	public Person getPerson() {
		return person;
	}
	public void setPerson(Person person) {
		this.person = person;
		this.identifier = String.valueOf(person.getSoldierId()) + ": " + person.getContent();
		
	}
	public CandidateScore getScore() {
		return score;
	}
	public void setScore(CandidateScore score) {
		this.score = score;
	}
	protected String getIdentifer() {
		return this.identifier;
	}

	@Override
	public boolean equals(Object obj) {

		return this.toString().equals(obj.toString());
	}

	@Override
	public int hashCode() {
		
		return identifier.hashCode();
	}
	
	@Override
	public String toString() {
		return this.identifier;
	}
	
	
}
