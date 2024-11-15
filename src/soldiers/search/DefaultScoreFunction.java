package soldiers.search;

public class DefaultScoreFunction implements ScoreFunction {
	
	// ignore differences in regiment
	// ignore forenames

	public int calculate(CandidateScore score) {

		int total = score.getNumber() + score.getSurname() + score.getInitials() + score.getRank();
		if ( score.getNumber() != 0 ) total++;
		return total;
	}

}
