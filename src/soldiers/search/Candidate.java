package soldiers.search;

import soldiers.database.Person;

public class Candidate {

	private Person person;
	private CandidateScore score;

	public Person getPerson() {
		return person;
	}
	public void setPerson(Person person) {
		this.person = person;
	}
	public CandidateScore getScore() {
		return score;
	}
	public void setScore(CandidateScore score) {
		this.score = score;
	}
}
