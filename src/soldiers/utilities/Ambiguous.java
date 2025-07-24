package soldiers.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.database.SoldiersModel;
import soldiers.search.Candidate;
import soldiers.search.CandidateScore;

public class Ambiguous {

	public static void main(String[] args) throws IOException, IllegalArgumentException, SAXException, XPathExpressionException, ParseException, SQLException, TransformerException {


    	if ( args.length < 1 ) {
    		
    		System.err.println("Usage: Ambiguous <input-filename>");
    		System.exit(1);
    	}
		
     	String inputfile   = args[0];

		XmlUtils xmlutils = new XmlUtils();
				
		Document source = xmlutils.parse(new File(inputfile));
		XPath xpath = xmlutils.newXPath();
		XPathExpression personExpr = xpath.compile("//soldiers:person");
		XPathExpression candidateExpr = xpath.compile(".//soldiers:candidate");
		
		NodeList people = (NodeList) personExpr.evaluate(source.getDocumentElement(), XPathConstants.NODESET);
		
		Connection connection = ConnectionManager.getConnection();

		Map<String, Integer> rankMap = SoldiersModel.getRankOrdinals(connection);

		for ( int p = 0; p < people.getLength(); p++ ) {
			
			Element person = (Element) people.item(p);
			Person query = Soldiers.parsePerson(person);

			NodeList list = (NodeList) candidateExpr.evaluate(person, XPathConstants.NODESET);
			
			Set<Candidate> candidates = new HashSet<Candidate>();
			Map<Long, Element> candidateMap = new HashMap<Long, Element>();
			
			for ( int i = 0; i < list.getLength(); i++ ) {
				
				Element e = (Element) list.item(i);
				long sid = Long.parseLong(e.getAttribute("sid"));
				candidateMap.put(sid, e);
				Person possibile = SoldiersModel.getPerson(connection, sid);
				Candidate candidate = new Candidate();
				CandidateScore score = new CandidateScore();
				candidate.setScore(score);
				candidate.setPerson(possibile);
				candidates.add(candidate);
			}
			
			if ( candidates.size() > 0 ) {
				
				checkDeath(query, candidates);
				if ( candidates.size() > 1 )  checkInitials(query, candidates);
				if ( candidates.size() > 1 )  checkRegiment(query, candidates);
				if ( candidates.size() > 1 )  checkRanks(query, candidates, rankMap, 3);
				if ( candidates.size() > 1 )  checkForenames(query, candidates);
				
				Set<Long> results = new HashSet<Long>();
				for ( Candidate candidate: candidates )  results.add(candidate.getPerson().getSoldierId());
				
				Set<Long> unwanted = new HashSet<Long>();
				unwanted.addAll(candidateMap.keySet());
				unwanted.removeAll(results);
				
				for (long sid: unwanted ) {
					
					Element x = candidateMap.get(sid);
					x.getParentNode().removeChild(x);
				}
			}
		}
				
		connection.close();
		
		
		TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource domsource = new DOMSource(source);
		StreamResult result = new StreamResult(new FileOutputStream("output/filtered.xml"));	
		transformer.transform(domsource, result);	

	}

	
	public static void checkDeath(Person query, Collection<Candidate> candidates) throws SQLException {
		
		Date qdeath = query.getDeath();
		Date qdeathafter = query.getDiedafter();
		Date qdeathbefore = query.getDiedbefore();
		
		Set<Candidate> inconsistent = new HashSet<Candidate>();
 		Set<Candidate> consistent   = new HashSet<Candidate>();
		
		for ( Candidate candidate: candidates ) {
			
			Person possible = candidate.getPerson();
			
			if ( qdeath != null ) {
				
				if ( possible.getDeath() != null ) {
					
					System.out.println("** " + possible.getDeath());
					
					if ( possible.getDeath().equals(qdeath) )  consistent.add(candidate);
					if ( possible.getDiedbefore() != null && qdeath.after(possible.getDiedbefore()))  inconsistent.add(candidate);
					if ( possible.getDiedafter()  != null && qdeath.before(possible.getDiedafter()))  inconsistent.add(candidate);
				}
				
			}
			else {
				
				if ( qdeathafter != null ) {
					
					if ( possible.getDeath() != null && qdeathafter.after(possible.getDeath()) )  inconsistent.add(candidate);
					if ( possible.getDiedbefore() != null && qdeathafter.after(possible.getDiedbefore()) )  inconsistent.add(candidate);
				}
				
				if ( qdeathbefore != null ) {
					
					if ( possible.getDeath() != null && qdeathbefore.before(possible.getDeath()) ) inconsistent.add(candidate);
					if ( possible.getDiedafter() != null && qdeathbefore.before(possible.getDiedafter()))  inconsistent.add(candidate);
				}
				
			}
		}
				
		if ( consistent.size() > 0 )  candidates.retainAll(consistent);	
		else candidates.removeAll(inconsistent);
		
	}

	
	public static void checkInitials(Person query, Collection<Candidate> candidates) {
		
 		String qinitials = query.getInitials();
 		if ( qinitials == null || qinitials.length() == 0 ) return;
 		
		Set<Candidate> inconsistent = new HashSet<Candidate>();
 		Set<Candidate> consistent   = new HashSet<Candidate>();
 		
 		for ( Candidate candidate: candidates ) {
 			
			Person possible = candidate.getPerson();
			String cinitials = possible.getInitials();
			if ( cinitials == null )  break;
			
			if ( qinitials.equals(cinitials) )  consistent.add(candidate);
			else {
				
				String[] q = qinitials.split("\\s+");
				String[] c = cinitials.split("\\s+");
				
				Set<String> qset = new HashSet<String>();
				Set<String> cset = new HashSet<String>();
				Set<String> intersection = new HashSet<String>();
				
				qset.addAll(Arrays.asList(q));
				cset.addAll(Arrays.asList(c));
				
				intersection.addAll(qset);
				intersection.retainAll(cset);
				
				qset.removeAll(intersection);
				cset.removeAll(intersection);
				
				if (qset.size() > 0 && cset.size() > 0 )  inconsistent.add(candidate);
				else if ( cset.size() > 0 )  consistent.add(candidate);
			}
 		}
 		
 		if ( consistent.size() > 0 ) candidates.retainAll(consistent);
 		else candidates.removeAll(inconsistent);
 		
	}

	
	public static void checkForenames(Person query, Collection<Candidate> candidates) {
		
 		String qnames = query.getForenames();
 		if ( qnames == null ) return;
 		
		Set<Candidate> inconsistent = new HashSet<Candidate>();
 		Set<Candidate> consistent   = new HashSet<Candidate>();
 		
 		for ( Candidate candidate: candidates ) {
 			
			Person possible = candidate.getPerson();
			String cnames = possible.getForenames();
			if ( cnames == null )  break;
			
			if ( qnames.equals(cnames) )  consistent.add(candidate);
			else {
				
				String[] q = qnames.split("\\s+");
				String[] c = cnames.split("\\s+");
				
				Set<String> qset = new HashSet<String>();
				Set<String> cset = new HashSet<String>();
				Set<String> intersection = new HashSet<String>();
				
				qset.addAll(Arrays.asList(q));
				cset.addAll(Arrays.asList(c));
				
				intersection.addAll(qset);
				intersection.retainAll(cset);
				
				qset.removeAll(intersection);
				cset.removeAll(intersection);
				
				if (qset.size() > 0 && cset.size() > 0 )  inconsistent.add(candidate);
				else if ( cset.size() > 0 )  consistent.add(candidate);
			}
 		}
 		
 		if ( consistent.size() > 0 ) candidates.retainAll(consistent);
 		else candidates.removeAll(inconsistent);
 		
	}

	
	public static void checkRegiment(Person query, Collection<Candidate> candidates) {
		
		Set<String> qset = getRegiments(query);
		if ( qset.size() == 0 )  return;
 		
 		Set<Candidate> consistent = new HashSet<Candidate>();
 		
 		for ( Candidate candidate: candidates ) {
 			
 			Set<String> cset = getRegiments(candidate.getPerson());
 			cset.retainAll(qset);
 			if ( cset.size() > 0 )  consistent.add(candidate);
 		}
		
		if ( consistent.size() > 0 ) candidates.retainAll(consistent);
	}

	
	public static void checkRanks(Person query, Collection<Candidate> candidates, Map<String, Integer> rankMap, int delta) {
		
		Set<Integer> qset = getRanks(query, rankMap);
		if ( qset.size() == 0 )  return;
		 		
 		Set<Candidate> consistent   = new HashSet<Candidate>();
 		Set<Candidate> inconsistent = new HashSet<Candidate>();
 		
 		for ( Candidate candidate: candidates ) {
 			
 			Set<Integer> cset = getRanks(candidate.getPerson(), rankMap);
 			Set<Integer> intersection = new HashSet<Integer>();
 			intersection.addAll(cset);
 			intersection.retainAll(qset);
 			
 			if ( intersection.size() > 0 )  consistent.add(candidate);
 			else {
 				
 				int minRankDistance = Integer.MAX_VALUE;
 				
 				for (int qnum: qset ) {
 					
 					for ( int cnum: cset ) {
 						
 						int rankDistance = Math.abs(qnum - cnum);
 						if ( rankDistance < minRankDistance )  minRankDistance = rankDistance;
 					}
 				}
 				
				if ( minRankDistance <= delta ) consistent.add(candidate);
				else if ( minRankDistance > delta + 1 ) inconsistent.add(candidate);

 			}
 		}
		
		if ( consistent.size() > 0 ) candidates.retainAll(consistent);
		else candidates.removeAll(inconsistent);
	}

	
	public static Set<String> getRegiments(Person soldier) {
		
		Set<String> results = new HashSet<String>();
		
		Set<Service> service = soldier.getService();
		
		for ( Service record: service ) {
			
			if ( record.getRegiment() != null ) results.add(record.getRegiment().replaceAll("\\s+", " ").trim().toLowerCase());
		}
		
		
		return results;
	}

	
	public static Set<Integer> getRanks(Person soldier, Map<String, Integer> rankMap) {
		
		Set<Integer> results = new HashSet<Integer>();
		
		Set<Service> service = soldier.getService();
		
		for ( Service record: service ) {
			
			String rank = record.getRank();
			
			if ( !record.getRank().equals("UNK") ) {
				
				Integer ordinal = rankMap.get(record.getRank());
				if ( ordinal != null ) results.add(ordinal);
				else System.err.println("Unknown rank: " + rank);
			}
		}
				
		return results;
	}

}
