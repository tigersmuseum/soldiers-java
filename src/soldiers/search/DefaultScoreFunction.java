package soldiers.search;

public class DefaultScoreFunction implements ScoreFunction {
	
	// ignore differences in regiment

	public int calculate(CandidateScore score) {

		int total = score.getNumber() + score.getSurname() + score.getInitials() + score.getForenames() + score.getRank();
		return total;
	}

}
