package soldiers.search;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.text.similarity.LevenshteinDistance;

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.database.SoldiersModel;
import soldiers.utilities.ConnectionManager;

public class PersonFinder {
	
	private StringEncoder encoder;
	private Map<String, List<String>> similarNameMap;
	Connection connection;
	Map<String, Integer> rankMap;
	
	public PersonFinder() {
		
		rankMap = SoldiersModel.getRankOrdinals(ConnectionManager.getConnection());
	}

	public void enableSimilarNameMatching(StringEncoder encoder, Map<String, List<String>> similarNameMap) {
		
		this.encoder = encoder;
		this.similarNameMap = similarNameMap;		
	}
	
	public List<Candidate> findMatches(Person p, Connection connection) {

		List<Candidate> candidates = new ArrayList<Candidate>();
		
		List<Person> results = new ArrayList<Person>();
		results.addAll(SoldiersModel.getCandidatesForNumberName(connection, p));
		
		if ( results.size() == 0 ) {
			
			if ( ! p.getService().isEmpty() ) {
				
				Set<Service> serviceSet = p.getService();
				
				for ( Service service: serviceSet ) {
					
					String number = service.getNumber();
					
					if ( number.length() > 0 ) {
						
						//results.addAll(SoldiersModel.getCandidatesForExactNumber(connection, p));
						results.addAll(SoldiersModel.getCandidatesForNumber(connection, p));
					}
				}				
			}
			
			List<String> names;
			try {
				names = similarNameMap.get(encoder.encode(p.getSurname()));

				if ( names!= null ) {
					
					for ( String name: names ) {
						
						Person x = new Person();
						x.setSurname(name);
						results.addAll(SoldiersModel.getCandidatesForSurname(connection, x));						
					}
				}
			}
			catch (EncoderException e) {
				e.printStackTrace();
			}			
		}
		
		
		for ( Person r: results ) {
			
			CandidateScore score = scoreCandidate(p, r);
			Candidate candidate = new Candidate();
			candidate.setPerson(r);
			candidate.setScore(score);
			
			candidates.add(candidate);
		}
		
		CandidateComparator comparator = new CandidateComparator();
		candidates.sort(comparator);
		
		System.out.println(candidates.size() + " matches");
		if ( candidates.size() > 0 ) {
			
			Candidate candidate = candidates.iterator().next();
			System.out.println("best - " + candidate.getPerson() + "; score = " + candidate.getScore().getOverallScore());
		}

		return candidates;
	}


	public CandidateScore scoreCandidate(Person query, Person candidate) {
		
		LevenshteinDistance distance = new LevenshteinDistance();
		CandidateScore score = new CandidateScore();
		
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
						
						if      ( qnumber.contains("/") && !cnumber.contains("/") ) qnumber = qnumber.substring(qnumber.lastIndexOf("/") + 1);
						else if ( cnumber.contains("/") && !qnumber.contains("/") ) cnumber = cnumber.substring(cnumber.lastIndexOf("/") + 1);
						
						numberDist = distance.apply(qnumber, cnumber);
						
						// add a penalty of 1 to the score if lengths of query and candidate service numbers don't match		
						//numberDist += qnumber.length() == cnumber.length() ? 0 : 1;
					}
				/*	else if ( qnumber.length() == 0 && cnumber.length() > 0 ) {
					// Or add 1 if the query doesn't have a number but the candidate does
						numberDist++;
					}*/
					
					// REGIMENT

					String qregiment = qservice.getRegiment();
					String cregiment = cservice.getRegiment();
					
					regimentDist = qregiment != null && qregiment.equals(cregiment) ? 0 : 1;
					
					// RANK

					rankDist = 0;
					
					if ( qservice.getRank() != null && !qservice.getRank().equals("UNK") && rankMap.get(qservice.getRank()) != null ) {
						
						// add one to the score if the candidate rank is different from the rank in the query
						int qrnum = rankMap.get(qservice.getRank());
						int crnum = rankMap.get(cservice.getRank());
						if ( crnum != qrnum ) rankDist += 1;
					}
					
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
