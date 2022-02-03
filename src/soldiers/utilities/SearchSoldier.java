package soldiers.utilities;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.database.SoldiersModel;
import soldiers.search.Candidate;
import soldiers.search.CandidateScore;
import soldiers.search.MakeEncoderMap;
import soldiers.search.CandidateComparator;

public class SearchSoldier {
	
	static Map<String, Integer> rankMap;

	public static void main(String[] args) throws TransformerConfigurationException, SAXException, IllegalArgumentException, FileNotFoundException {

		Person p = new Person();
		Service svc = new Service();
		
		p.setSurname("BASTON");
		//svc.setNumber("125157");
		p.setInitials("T J");

		//svc.setRank("L/Cpl");
		p.addService(svc);
				
		List<Person> results = SearchSoldier.checkIdentity(p, ConnectionManager.getConnection());
		Iterator<Person> x = results.iterator();
		
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
        serializer = tf.newTransformerHandler();
        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setResult(new StreamResult(new FileOutputStream("output/results.xml")));

		serializer.startDocument();
		serializer.startElement(SoldiersModel.XML_NAMESPACE, "list", "list", new AttributesImpl());


		while ( x.hasNext() ) {
			
			Person r = SoldiersModel.getPerson(ConnectionManager.getConnection(), x.next().getSoldierId());
			System.out.println("db: " + r);
			r.serializePerson(serializer);
		}
		
		serializer.endElement(SoldiersModel.XML_NAMESPACE, "list", "list");
		serializer.endDocument();
		
		x = results.iterator();
		
		while ( x.hasNext() ) {
			
			scoreCandidate(p, x.next());
		}
		
	}


	public static List<Person> checkIdentity(Person p, Connection connection) {

		List<Person> results = SoldiersModel.getCandidatesForNumberName(connection, p);
		
		Set<Service> service = p.getService();
		
		if ( service.size() > 0 ) {
			
			Service svc = service.iterator().next();
			if (svc.getNumber() != null && svc.getNumber().length() > 0) results.addAll(SoldiersModel.getCandidatesForExactNumber(connection, p));
		}
		
		if ( filterMatchingNumber(p, results).size() == 0 ) {
			
			results.addAll(SoldiersModel.getCandidatesForSurname(connection, p));
		}
		//results.addAll(SoldiersModel.getCandidatesForNameInitials(connection, p));
		
		results.addAll(checkIdentityOfficer(p, connection));

		System.out.println(".X....." + results.size());
		
		return filterMatches(p, results, 4);
		//return results;

	}


	public static List<Candidate> findMatches(Person p, Connection connection) {

		List<Candidate> candidates = new ArrayList<Candidate>();
		
		//Set<Person> results = SoldiersModel.getCandidatesForNumberName(connection, p);
		List<Person> results = SoldiersModel.getCandidatesForSurname(connection, p);
//		results.addAll(SoldiersModel.getCandidatesForExactNumber(connection, p));
		
		if ( results.size() == 0 ) {
			
			Metaphone encoder = new Metaphone();
			Map<String, List<String>> soundMap = MakeEncoderMap.getSoundMap(encoder, connection);
			
			List<String> names = soundMap.get(encoder.encode(p.getSurname()));
			
			if ( names!= null ) {
								
				for ( String name: names ) {
					
					Person x = new Person();
					x.setSurname(name);
					results.addAll(SoldiersModel.getCandidatesForSurname(connection, x));
					
				}
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
		if ( candidates.size() > 0 ) System.out.println("best - " + candidates.iterator().next().getPerson());

		return candidates;
	}

	
	public static List<Person> checkIdentityOfficer(Person p, Connection connection) {

		
		if (rankMap == null) {
			rankMap = SoldiersModel.getRankOrdinals(ConnectionManager.getConnection());
			rankMap.put("", 0);
		}
		
		List<Person> results = new ArrayList<Person>();
		
		Set<Service> service = p.getService();
		
		if ( service.size() > 0 ) {
			
			Service svc = service.iterator().next();
			
			if (rankMap.get(svc.getRank()) == null ) {
				
				System.err.println("person is " + p);
				System.err.println("service is " + svc);
			}
			
			if (rankMap.get(svc.getRank()) != null && rankMap.get(svc.getRank()) > 8 ) results.addAll(SoldiersModel.getCandidatesForSurname(connection, p));
		}
		
		return filterMatches(p, results, 1);
		//return results;

	}
	
	
	public static List<Person> filterMatches(Person query, List<Person> results, Integer maxscore) {
		
		LevenshteinDistance distance = new LevenshteinDistance();
		Soundex soundex = new Soundex();
				
		if (rankMap == null) {
			rankMap = SoldiersModel.getRankOrdinals(ConnectionManager.getConnection());
			rankMap.put("", 0);
		}

		if ( query.getService().size() == 0 ) return results;
		System.out.println("QQQQQQQQQQ " + query.getContent());

		List<Person> filtered = new ArrayList<Person>();
		
		Map<Integer, Set<Person>> scores = new HashMap<Integer, Set<Person>>();
		
		for ( Person candidate: results ) {
			
			Set<Service> service = query.getService();
			Service svcq = service.iterator().next();

			service = candidate.getService();
			Service svcc = service.iterator().next();
			
			String qnum = svcq.getNumber();
			String cnum = svcc.getNumber();
			
			String qsurname = query.getSurname();
			String csurname = candidate.getSurname();
			String qinitials = query.getInitials();
			String cinitials = candidate.getInitials();
			
			String qsound = soundex.encode(qsurname);
			String csound = soundex.encode(csurname);
			
			if (qinitials == null)  qinitials= "";
			if (cinitials == null)  cinitials= "";
			
			int surnamedist = distance.apply(qsurname, csurname);
			int numberdist  = distance.apply(qnum.replace("/", ""), cnum.replace("/", ""));
			int sounddist   = distance.apply(qsound, csound);
			
			//System.out.println(qsound + " = " + csound);
			
			if ( qinitials.length() < cinitials.length() ) {
				
				cinitials = cinitials.substring(0, qinitials.length());
			}
			else if ( qinitials.length() > cinitials.length() ) {
				
				qinitials = qinitials.substring(0, cinitials.length());
			}
			
			int initdist = distance.apply(qinitials, cinitials);
			
			
			//System.out.println("aa: " + svcc);

			//if ( qnum.length() >= 2 && qnum.equals(cnum) && surnamedist < 4 ) {
			if ( qnum.length() >= 2 && qnum.equals(cnum) && svcc.getRegiment() != null && (svcc.getRegiment().startsWith("Hampshire") || svcc.getRegiment().startsWith("Labour")) ) {
				
				//getCandidateSet(surnamedist, scores).add(candidate);
				getCandidateSet(Math.min(surnamedist, sounddist+1), scores).add(candidate);
				System.out.println("A: " + candidate.getContent() + " = " + surnamedist);
			}
			
			else if ( (surnamedist < 2 || soundex.encode(qsurname).equals(soundex.encode(csurname))) && qnum.length() > 0 && cnum.length() > 0 && numberdist <= 2 ) {
				
				if ( qsound.equals(csound) ) surnamedist = 0;
				getCandidateSet(numberdist + surnamedist + initdist, scores).add(candidate);
			//	System.out.println("B: " + candidate.getContent() + " = " + numberdist + surnamedist + initdist);
			}
			else if ( qnum.length() == 0 && qinitials != null && distance.apply(qsurname, csurname) <= 2 && qinitials.equals(cinitials)) {
				
				//System.out.println("[" + svcq.getRank() + "]");
				int ndist = svcq.getRank() == null || rankMap.get(svcq.getRank()) < 8 ? 1 : cnum.length();
				//filtered.add(candidate);				
				getCandidateSet(surnamedist + ndist, scores).add(candidate);				
			//	System.out.println("C: " + candidate.getContent() + " = " + surnamedist);
			}
			else if ( qnum.equals(cnum) && qsurname.equals(csurname) && qinitials.length() >= 3 && initdist <= 1 ) {
				
				getCandidateSet(initdist, scores).add(candidate);				
				//filtered.add(candidate);								
			//	System.out.println("D: " + candidate.getContent() + " = " + initdist);
			}
			else {
				//System.out.println("rejected: " + candidate.getContent());
			}
		}
		
		List<Integer> s = new ArrayList<Integer>();
		s.addAll(scores.keySet());
		Collections.sort(s);
		
		System.out.println("RESULT SETS: " + s);

		if ( !s.isEmpty() ) {
			
			int r = s.iterator().next();
			System.out.println(r + " = " + scores.get(r).size());
			 if ( r <= maxscore) filtered.addAll(scores.get(r));
		}
		
		return filtered;
	}
	
	
	private static Set<Person> getCandidateSet( int key, Map <Integer,Set<Person>> map ) {
		
		Set<Person> candidates = map.get(key);
		
		if ( candidates == null ) {
			
			candidates = new HashSet<Person>();
			map.put(key, candidates);
		}
		
		return candidates;
	}

	public static List<Person> filterMatchingNumber(Person query, List<Person> results) {
		
		LevenshteinDistance distance = new LevenshteinDistance();
		Soundex soundex = new Soundex();

		List<Person> filtered = new ArrayList<Person>();
		
		for ( Person candidate: results ) {
			
			String qsurname = query.getSurname();
			String csurname = candidate.getSurname();
			int surnamedist = distance.apply(qsurname, csurname);
			
			//System.out.println(soundex.encode(qsurname) + " = " + soundex.encode(csurname));
			
			if ( soundex.encode(qsurname).equals(soundex.encode(csurname)) ) {
				
				filtered.add(candidate);
			}
			else if ( surnamedist <= 2 ) {
				
				filtered.add(candidate);
			}
		}
			
		return filtered;
	}
	
	public static CandidateScore scoreCandidate(Person query, Person candidate) {
		
		LevenshteinDistance distance = new LevenshteinDistance();
		Soundex soundex = new Soundex();
		
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
		
		System.out.println("number, dist: " + qnumber + ", " + cnumber + ", " + numberDist);
		
		// SURNAME
		
		String qsurname = query.getSurname();
		String csurname = candidate.getSurname();
		
		int surnameDist = distance.apply(qsurname, csurname);
		surnameDist += distance.apply(soundex.encode(qsurname), soundex.encode(csurname));
		
		System.out.println("surname, dist: " + qsurname + ", " + csurname + ", " + surnameDist);
		
		// INITIALS
		
		String qinitials = query.getInitials();
		String cinitials = candidate.getInitials();
		
		if (qinitials == null)  qinitials= "";
		if (cinitials == null)  cinitials= "";
		
		if ( qinitials.length() < cinitials.length() )			cinitials = cinitials.substring(0, qinitials.length());
		else if ( qinitials.length() > cinitials.length() )		qinitials = qinitials.substring(0, cinitials.length());
		
		int initialsDist = distance.apply(qinitials, cinitials);
		
		System.out.println("initials, dist: " + qinitials + ", " + cinitials + ", " + initialsDist);
		
		// REGIMENT

		String qregiment = qservice.getRegiment();
		String cregiment = cservice.getRegiment();
		
		int regimentDist = qregiment != null && qregiment.equals(cregiment) ? 0 : 1;
		
		System.out.println("regiment, dist: " + qregiment + ", " + cregiment + ", " + regimentDist);
		
		System.out.println(" ---------------- ");
		
		CandidateScore score = new CandidateScore();
		
		score.setSurname(surnameDist);
		score.setNumber(numberDist);
		score.setInitials(initialsDist);
		score.setRegiment(regimentDist);
		
		return score;
	}
	
}
