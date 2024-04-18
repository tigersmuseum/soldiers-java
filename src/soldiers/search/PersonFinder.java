package soldiers.search;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
				
				String number = p.getService().iterator().next().getNumber();
				
				if ( number.length() > 0 ) {
					
					//results.addAll(SoldiersModel.getCandidatesForExactNumber(connection, p));
					results.addAll(SoldiersModel.getCandidatesForNumber(connection, p));
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
		
		// The query and candidate Person objects will have only one service record each ...	
		
		int numberDist = 0, regimentDist = 0, rankDist = 0, forenamesDist = 0;
		
		//scoreService(query, candidate, distance);
		
		if ( ! query.getService().isEmpty() ) {
			
			Service qservice = query.getService().iterator().next();
			Service cservice = candidate.getService().iterator().next();
			
			// SERVICE NUMBER
			
			String qnumber = qservice.getNumber();
			String cnumber = cservice.getNumber();
			
			// Add a penalty score if a service number was specified in the query
			if ( qnumber.length() > 0 ) {
				
				if      ( qnumber.contains("/") && !cnumber.contains("/") ) qnumber = qnumber.substring(qnumber.indexOf("/") + 1);
				else if ( cnumber.contains("/") && !qnumber.contains("/") ) cnumber = cnumber.substring(cnumber.indexOf("/") + 1);
				
				numberDist = distance.apply(qnumber, cnumber);
				
				// add a penalty of 1 to the score if lengths of query and candidate service numbers don't match		
				numberDist += qnumber.length() == cnumber.length() ? 0 : 1;
			}
			else if ( qnumber.length() == 0 && cnumber.length() > 0 ) {
			// Or add 1 if the query doesn't have a number but the candidate does
				numberDist++;
			}
			
			// REGIMENT

			String qregiment = qservice.getRegiment();
			String cregiment = cservice.getRegiment();
			
			regimentDist = qregiment != null && qregiment.equals(cregiment) ? 0 : 1;
			
			// RANK

			rankDist = 0;
			
			if ( qservice.getRank() != null && rankMap.get(qservice.getRank()) != null ) {
				
				// add one to the score if the candidate rank is lower than the rank in the query
				int qrnum = rankMap.get(qservice.getRank());
				int crnum = rankMap.get(cservice.getRank());
				if ( crnum < qrnum ) rankDist += 1;
			}
		}
		
		// SURNAME
		
		String qsurname = query.getSurname();
		String csurname = candidate.getSurname();
		
		int surnameDist = distance.apply(qsurname, csurname);
		
		// INITIALS
		
		String qinitials = query.getInitials();
		String cinitials = candidate.getInitials();
		
		if (qinitials == null)  qinitials= "";
		if (cinitials == null)  cinitials= "";
		
		if ( qinitials.length() < cinitials.length() )			cinitials = cinitials.substring(0, qinitials.length());
		else if ( qinitials.length() > cinitials.length() )		qinitials = qinitials.substring(0, cinitials.length());
		
		int initialsDist = distance.apply(qinitials, cinitials);
		
		// FORENAMES
		
		String qfname = query.getForenames();
		String cfname = candidate.getForenames();
		
		if ( qfname != null  && cfname != null ) {
			
			if ( !qfname.toUpperCase().equals(cfname.toUpperCase()) )  forenamesDist = 1;
		}		
		
		CandidateScore score = new CandidateScore();
		
		score.setSurname(surnameDist);
		score.setNumber(numberDist);
		score.setInitials(initialsDist);
		score.setRegiment(regimentDist);
		score.setRank(rankDist);
		score.setForenames(forenamesDist);
		
		return score;
	}

}
