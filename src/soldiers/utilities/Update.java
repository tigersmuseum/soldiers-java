package soldiers.utilities;

import java.io.File;
import java.sql.Connection;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import soldiers.database.Person;
import soldiers.database.Service;
import soldiers.database.SoldiersModel;
import soldiers.database.SoldiersNamespaceContext;

public class Update {

	public static void main(String[] args) throws XPathExpressionException, ParseException {
		
		// parameterize?
		// work off @sid attribute on "person" or "candidate? (compare with Insert)

	    XmlUtils xmlutils = new XmlUtils();
		//Document doc = xmlutils.parse(new File("/F:/Museum/results.xml"));
		Document doc = xmlutils.parse(new File("output/results.xml"));
		
		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();

	    xpath.setNamespaceContext(new SoldiersNamespaceContext());
		XPathExpression expr = xpath.compile(".//soldiers:person");
		NodeList list = (NodeList) expr.evaluate(doc.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int i = 0; i < list.getLength(); i++ ) {
			
			Person person = Soldiers.parsePerson((Element) list.item(i));
			
			System.out.println(person);
			
			compareToDatabase(person);
			
			System.out.println("-----------");
		}
	}
	
	
	public static void compareToDatabase(Person person) {
		
		Long sid = person.getSoldierId();
		
		Connection connection = ConnectionManager.getConnection();
		Person known = SoldiersModel.getPerson(connection, sid);
		
		if ( known.getSoldierId() < 0 ) {
			
			System.err.println("No soldier with SID = " + sid);
		}
		
		else {
			
			if ( !person.getSurname().equals(known.getSurname()) ) {
			
				System.out.println(person.getSurname() + " != " + known.getSurname());
				SoldiersModel.updateSurname(connection, person);
			}
			if ( person.getForenames() != null && !person.getForenames().equals(known.getForenames()) ) {
			
				System.out.println(person.getForenames() + " != " + known.getForenames());				
				SoldiersModel.updateForenames(connection, person);
			}
			if ( !person.getInitials().equals(known.getInitials()) ) {
				
				System.out.println(person.getInitials() + " != " + known.getInitials());				
				SoldiersModel.updateInitials(connection, person);
			}
			
			
			// birth
			
			if ( person.getBirth() != null && !person.getBirth().equals(known.getBirth()) ) {
				
				System.out.println("UPDATE birth: " + person.getBirth() + " != " + known.getBirth());
				SoldiersModel.updateBirth(connection, person);
			}
			
			// born before
			
			if ( person.getBornbefore() != null && !person.getBornbefore().equals(known.getBornbefore()) ) {
				
				System.out.println("UPDATE bornbefore: " + person.getBornbefore() + " != " + known.getBornbefore());				
				SoldiersModel.updateBornBefore(connection, person);
			}
			
			// born after
			
			if ( person.getBornafter() != null && !person.getBornafter().equals(known.getBornafter()) ) {
				
				System.out.println("UPDATE bornafter: " + person.getBornafter() + " != " + known.getBornafter());				
				SoldiersModel.updateBornAfter(connection, person);
			}
			
			// death
			
			if ( person.getDeath() != null && !person.getDeath().equals(known.getDeath()) ) {
				
				System.out.println("UPDATE death: " + person.getDeath() + " != " + known.getDeath());				
				SoldiersModel.updateDeath(connection, person);
			}
			
			// died before
			
			if ( person.getDiedbefore() != null && !person.getDiedbefore().equals(known.getDiedbefore()) ) {
				
				SoldiersModel.updateDiedBefore(connection, person);
			}
			
			// died after
			
			if ( person.getDiedafter() != null && !person.getDiedafter().equals(known.getDiedafter()) ) {
				
				SoldiersModel.updateDiedAfter(connection, person);
			}
			
			Map<String, Service> serviceMap = new HashMap<String, Service>();
			
			for ( Service service: known.getService() ) {
				
				// "SID", "NUM", "RANK_ABBREV", "REGIMENT", "BEFORE"
				String key = String.format("%d: %s %s %s", service.getSoldierId(), service.getNumber(), service.getRank(), service.getRegiment());
				System.out.println("KNOWN: " + key);
				serviceMap.put(key, service);
			}
			
			for ( Service service: person.getService() ) {
				
				String key = String.format("%d: %s %s %s", service.getSoldierId(), service.getNumber(), service.getRank(), service.getRegiment());
				System.out.println("TEST: " + key);
				Service knownService = serviceMap.get(key);
				
				if ( knownService == null ) {
					
					System.out.println("INSERT - " + service);
					SoldiersModel.insertService(connection, service);
				}
				else {
					
					if ( service.getUnit() != null && ! service.getUnit().equals(knownService.getUnit()) )  {
						System.out.println("update unit to " + service.getUnit());
						SoldiersModel.updateUnit(connection, service);
					}
					if ( service.getAfter() != null && !service.getAfter().equals(knownService.getAfter()) ) {
						System.out.println("update after to " + service.getAfter());
						SoldiersModel.updateServiceAfter(connection, service, knownService);
					}
					if ( service.getBefore() != null && !service.getBefore().equals(knownService.getBefore()) )  {
						System.out.println("update before to " + service.getBefore() + " != " + knownService.getBefore());
						SoldiersModel.updateServiceBefore(connection, service, knownService);
					}
				}
			}
		}
	}

}
