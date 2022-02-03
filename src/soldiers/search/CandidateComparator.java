package soldiers.search;

import java.util.Comparator;

public class CandidateComparator implements Comparator<Candidate> {

	@Override
	public int compare(Candidate a, Candidate b) {

		return a.getScore().getOverallScore() - b.getScore().getOverallScore();
	}

}
