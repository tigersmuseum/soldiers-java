package soldiers.search;

public class CandidateScore {
	
	private int surname = 0;
	private int forenames = 0;
	private int number = 0;
	private int initials = 0;
	private int regiment = 0;
	private int rank = 0;
	
	private ScoreFunction function;
	
	
	public CandidateScore() {
		
		// set a default scoring function
		this.function = new DefaultScoreFunction();
	}
	
	
	public int getSurname() {
		return surname;
	}
	public void setSurname(int surname) {
		this.surname = surname;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getInitials() {
		return initials;
	}
	public void setInitials(int initials) {
		this.initials = initials;
	}
	public int getRegiment() {
		return regiment;
	}
	public void setRegiment(int regiment) {
		this.regiment = regiment;
	}
	public void setFunction(ScoreFunction function) {
		this.function = function;
	}
	
	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getForenames() {
		return forenames;
	}
	public void setForenames(int forenames) {
		this.forenames = forenames;
	}


	public int getOverallScore() {
		
		return function.calculate(this);
	}
}
