package soldiers.search;

public class SimpleSumScoreFunction implements ScoreFunction {

	public int calculate(CandidateScore score) {

		int total = score.getNumber() + score.getSurname() + score.getInitials() + score.getRegiment() + score.getRank();
		return total;
	}

}
