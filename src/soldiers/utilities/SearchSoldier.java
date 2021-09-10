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

import org.apache.commons.codec.language.Soundex;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.database.SoldiersModel;

public class SearchSoldier {

	public static void main(String[] args) throws TransformerConfigurationException, SAXException, IllegalArgumentException, FileNotFoundException {

		Person p = new Person();
		Service svc = new Service();
		
		p.setSurname("ATKEY");
		//svc.setNumber("2629");
		p.setInitials("W");
		
		p.addService(svc);
				
		Set<Person> results = SearchSoldier.checkIdentity(p, ConnectionManager.getConnection());
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
	}


	public static Set<Person> checkIdentity(Person p, Connection connection) {

		Set<Person> results = SoldiersModel.getCandidatesForNumberName(connection, p);
		
		Set<Service> service = p.getService();
		
		if ( service.size() > 0 ) {
			
			Service svc = service.iterator().next();
			if (svc.getNumber() != null && svc.getNumber().length() > 0) results.addAll(SoldiersModel.getCandidatesForExactNumber(connection, p));
		}
		
		//results.addAll(SoldiersModel.getCandidatesForNameInitials(connection, p));
		results.addAll(SoldiersModel.getCandidatesForSurname(connection, p));

		System.out.println(".X....." + results.size());
		
		return filterMatches(p, results);
		//return results;

	}
	
	
	public static Set<Person> filterMatches(Person query, Set<Person> results) {
		
		LevenshteinDistance distance = new LevenshteinDistance();
		Soundex soundex = new Soundex();
		
		if ( query.getService().size() == 0 ) return results;
		System.out.println("QQQQQQQQQQ " + query.getContent());

		Set<Person> filtered = new HashSet<Person>();
		
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
			
			if (qinitials == null)  qinitials= "";
			if (cinitials == null)  cinitials= "";
			
			int surnamedist = distance.apply(qsurname, csurname);
			int numberdist  = distance.apply(qnum.replace("/", ""), cnum.replace("/", ""));
			int initdist = distance.apply(qinitials, cinitials);
			

			if ( qnum.length() >= 4 && qnum.equals(cnum) && surnamedist < 4 ) {
				
				//filtered.add(candidate);				
				getCandidateSet(surnamedist, scores).add(candidate);
			//	System.out.println("A: " + candidate.getContent() + " = " + surnamedist);
			}
			
			else if ( (surnamedist < 2 || soundex.encode(qsurname).equals(csurname)) && qnum.length() > 0 && cnum.length() > 0 && numberdist <= 2 ) {
				
				getCandidateSet(numberdist + surnamedist + initdist, scores).add(candidate);
			//	System.out.println("B: " + candidate.getContent() + " = " + numberdist + surnamedist + initdist);
			}
			else if ( qnum.length() == 0 && qinitials != null && distance.apply(qsurname, csurname) <= 2 && qinitials.equals(cinitials)) {
				
				//filtered.add(candidate);				
				getCandidateSet(surnamedist, scores).add(candidate);				
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
			filtered.addAll(scores.get(r));
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

}
