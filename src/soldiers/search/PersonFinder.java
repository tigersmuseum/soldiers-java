package soldiers.search;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.text.similarity.LevenshteinDistance;

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.database.SoldiersModel;

public class PersonFinder {
	
	private StringEncoder encoder;
	private Map<String, List<String>> similarNameMap;
	Connection connection;

	public PersonFinder() {
		
	}

	public void enableSimilarNameMatching(StringEncoder encoder, Map<String, List<String>> similarNameMap) {
		
		this.encoder = encoder;
		this.similarNameMap = similarNameMap;		
	}
	
	public List<Candidate> findMatches(Person p, Connection connection) {

		List<Candidate> candidates = new ArrayList<Candidate>();
		
		Set<Person> results = new HashSet<Person>();
		//List<Person> results = SoldiersModel.getCandidatesForNumberName(connection, p);
		
		//if ( results.size() == 0 ) results = SoldiersModel.getCandidatesForSurname(connection, p);
		//results.addAll(SoldiersModel.getCandidatesForExactNumber(connection, p));
				
		String number = p.getService().iterator().next().getNumber();
		if ( number.length() > 0 ) results.addAll(SoldiersModel.getCandidatesForExactNumber(connection, p));
		
//		if ( results.size() == 0 ) {
			
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
//		}
		
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
		if ( candidates.size() > 0 ) System.out.println("best - " + candidates.iterator().next().getPerson());

		return candidates;
	}


	public CandidateScore scoreCandidate(Person query, Person candidate) {
		
		LevenshteinDistance distance = new LevenshteinDistance();
		
		// The query and candidate Person objects will have only one service record each ...
		
		Service qservice = query.getService().iterator().next();
		Service cservice = candidate.getService().iterator().next();
		
		// SERVICE NUMBER
		
		String qnumber = qservice.getNumber();
		String cnumber = cservice.getNumber();
		
		if      ( qnumber.contains("/") && !cnumber.contains("/") ) qnumber = qnumber.substring(qnumber.indexOf("/") + 1);
		else if ( cnumber.contains("/") && !qnumber.contains("/") ) cnumber = cnumber.substring(cnumber.indexOf("/") + 1);
		
		int numberDist = distance.apply(qnumber, cnumber);
		
		// add a penalty of 1 to the score if lengths of query and candidate service numbers don't match		
		numberDist += qnumber.length() == cnumber.length() ? 0 : 1;
		
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
		
		// REGIMENT

		String qregiment = qservice.getRegiment();
		String cregiment = cservice.getRegiment();
		
		int regimentDist = qregiment != null && qregiment.equals(cregiment) ? 0 : 1;
		
		// RANK

		int rankDist = qservice.getRank() != null && qregiment != null && qservice.getRank().equals(cservice.getRank()) ? 0 : 1;
		
		CandidateScore score = new CandidateScore();
		
		score.setSurname(surnameDist);
		score.setNumber(numberDist);
		score.setInitials(initialsDist);
		score.setRegiment(regimentDist);
		score.setRank(rankDist);
		
		return score;
	}

	
}
