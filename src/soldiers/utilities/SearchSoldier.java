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
		
		p.setSurname("DEAN");
		svc.setNumber("8041");
		p.setInitials("H G");
		
		p.addService(svc);
		
		Set<Person> results = SoldiersModel.getCandidatesForNumberName(ConnectionManager.getAccessConnection(), p);
		
		if (svc.getNumber() != null) results.addAll(SoldiersModel.getCandidatesForExactNumber(ConnectionManager.getAccessConnection(), p));
		results.addAll(SoldiersModel.getCandidatesForNameInitials(ConnectionManager.getAccessConnection(), p));

		System.out.println("......" + results.size());
		
		Iterator<Person> x = results.iterator();
//		Iterator<Person> x = filterMatches(p, results).iterator();
		
        SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler serializer;
        serializer = tf.newTransformerHandler();
        serializer.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setResult(new StreamResult(new FileOutputStream("output/results.xml")));

		serializer.startDocument();
		serializer.startElement("", "results", "results", new AttributesImpl());


		while ( x.hasNext() ) {
			
			Person r = SoldiersModel.getPerson(ConnectionManager.getAccessConnection(), x.next().getSoldierId());
			System.out.println("db: " + r);
			r.serializePerson(serializer);
		}
		
		serializer.endElement("", "results", "results");
		serializer.endDocument();
	}


	public static Set<Person> checkIdentity(Person p, Connection connection) {

		Set<Person> results = SoldiersModel.getCandidatesForNumberName(connection, p);
		
		Set<Service> service = p.getService();
		
		if ( service.size() > 0 ) {
			
			Service svc = service.iterator().next();
			if (svc.getNumber() != null) results.addAll(SoldiersModel.getCandidatesForExactNumber(connection, p));
		}
		
		results.addAll(SoldiersModel.getCandidatesForNameInitials(connection, p));

		System.out.println("......" + results.size());
		
		return filterMatches(p, results);
		//return results;

	}
	
	
	public static Set<Person> filterMatches(Person query, Set<Person> results) {
		
		LevenshteinDistance distance = new LevenshteinDistance();
		Soundex soundex = new Soundex();
		
		if ( query.getService().size() == 0 ) return results;

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
			
			int surnamedist = distance.apply(qsurname, csurname);

			if ( qnum != null && qnum.length() > 4 && qnum.equals(cnum) && distance.apply(qsurname, csurname) < 3 ) {
				
				filtered.add(candidate);				
				getCandidateSet(surnamedist, scores).add(candidate);				
			}
			
			else if ( (distance.apply(qsurname, csurname) < 2 || soundex.encode(qsurname).equals(csurname)) && qnum != null && cnum != null && distance.apply(qnum, cnum) < 3 ) {
				
				getCandidateSet(distance.apply(qnum, cnum), scores).add(candidate);
			}
			else if ( qnum == null && cnum == null && qinitials != null && distance.apply(qsurname, csurname) < 2 && qinitials.equals(cinitials)) {
				
				filtered.add(candidate);				
			}
		}
		
		List<Integer> s = new ArrayList<Integer>();
		s.addAll(scores.keySet());
		Collections.sort(s);
		if ( !s.isEmpty() ) filtered.addAll(scores.get(s.iterator().next()));
		
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
