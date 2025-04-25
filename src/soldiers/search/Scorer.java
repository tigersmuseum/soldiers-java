package soldiers.search;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.text.similarity.LevenshteinDistance;

import soldiers.database.Person;
import soldiers.database.Service;

public class Scorer {

	private LevenshteinDistance distance;
	private CandidateScore score;

	public Scorer() {
		
		distance = new LevenshteinDistance();
		score = new CandidateScore();
	}
	
	public CandidateScore scoreCandidate(Person query, Person candidate) {
		
		// SERVICE
		
		scoreService(query.getService(), candidate.getService(), distance, score);
		
		// SURNAME
		
		String qsurname = query.getSurname();
		String csurname = candidate.getSurname();
		
		int surnameDist = ( qsurname != null && csurname != null ) ? distance.apply(qsurname, csurname) : 0;
		
		// INITIALS
		
		String qinitials = query.getInitials();
		String cinitials = candidate.getInitials();
		
		if (qinitials == null)  qinitials= "";
		if (cinitials == null)  cinitials= "";
		
		if ( qinitials.length() < cinitials.length() )			cinitials = cinitials.substring(0, qinitials.length());
		else if ( qinitials.length() > cinitials.length() )		qinitials = qinitials.substring(0, cinitials.length());
		
		int initialsDist = distance.apply(qinitials, cinitials);
		
		// FORENAMES
		
		int forenamesDist = 0;
		String qfname = query.getForenames();
		String cfname = candidate.getForenames();
		
		if ( qfname != null  && cfname != null ) {
			
			if ( !qfname.toUpperCase().equals(cfname.toUpperCase()) )  forenamesDist = 1;
		}		

		score.setSurname(surnameDist);
		score.setInitials(initialsDist);
		score.setForenames(forenamesDist);

		return score;
	}

	private void scoreService(Set<Service> qset, Set<Service> cset, LevenshteinDistance distance, CandidateScore score) {
		
		int lowScore = Integer.MAX_VALUE;
		
		if ( ! qset.isEmpty() ) {

			Iterator<Service> qIter = qset.iterator();
			
			while ( qIter.hasNext() ) {
				
				Service qservice = qIter.next();
				Iterator<Service> cIter = cset.iterator();
				
				while ( cIter.hasNext() ) {
					
					int numberDist = 0, regimentDist = 0, rankDist = 0;

					Service cservice = cIter.next();
					String qnumber = qservice.getNumber();
					String cnumber = cservice.getNumber();
					
					// Add a penalty score if a service number was specified in the query
					if ( qnumber.length() > 0 ) {
						
						if      ( qnumber.contains("/") && !cnumber.contains("/") ) qnumber = qnumber.substring(qnumber.indexOf("/") + 1);
						else if ( cnumber.contains("/") && !qnumber.contains("/") ) cnumber = cnumber.substring(cnumber.indexOf("/") + 1);
						
						numberDist = distance.apply(qnumber, cnumber);
						
						// add a penalty of 1 to the score if lengths of query and candidate service numbers don't match		
						//numberDist += qnumber.length() == cnumber.length() ? 0 : 1;
					}
					else if ( qnumber.length() == 0 && cnumber.length() > 0 ) {
					// Or add 1 if the query doesn't have a number but the candidate does
						numberDist++;
					}
					
					// REGIMENT

					String qregiment = qservice.getRegiment().toLowerCase();
					String cregiment = cservice.getRegiment().toLowerCase();
					
					regimentDist = qregiment != null && qregiment.equals(cregiment) ? 0 : 1;
					
					// RANK

					rankDist = 0;
/*					
					if ( qservice.getRank() != null && !qservice.getRank().equals("UNK") && rankMap.get(qservice.getRank()) != null ) {
						
						// add one to the score if the candidate rank is different from the rank in the query
						int qrnum = rankMap.get(qservice.getRank());
						int crnum = rankMap.get(cservice.getRank());
						if ( crnum != qrnum ) rankDist += 1;
					}
*/					
					int total = numberDist + regimentDist + rankDist;

					if ( total < lowScore )  {
						
						lowScore = total;
						score.setNumber(numberDist);
						score.setRank(rankDist);
						score.setRegiment(regimentDist);
					}
				}
			}
		}
	}

}
