package soldiers.mentions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import soldiers.database.MentionsModel;
import soldiers.database.Person;
import soldiers.database.SoldiersModel;
import soldiers.utilities.ConnectionManager;
import soldiers.utilities.Soldiers;
import soldiers.utilities.XmlUtils;

public class Report {

	public static void main(String[] args) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException, SQLException {

		XmlUtils xmlutils = new XmlUtils();	
		Connection connection = ConnectionManager.getConnection();
		
		Set<String> sourcesDB = MentionsModel.getSources(connection);
		System.out.println("Sources in the database: " + sourcesDB);
		
		Document doc = xmlutils.readDocument(new FileInputStream("/C:/Users/Archive.SERLESHOUSE/eclipse-workspace/Research/data/sources.xml"));			 
		doc.normalize();
		Map<String, String> sourcesMap = Sources.getSourceMap(doc);
		Set<String> sources = sourcesMap.keySet();
		System.out.println("Sources in master list: " + sources);	
		
		sources.removeAll(sourcesDB);
		System.out.println("Sources not in database: " + sources);
		
		Map<String, Integer> counts = getCountsBySource(connection);
		System.out.println(counts);
		
		connection.close();
	}
	
	
	static void reportMentions(Map<Long, Set<Element>> candidateMap) throws ParseException {
		
		for ( long sid: candidateMap.keySet()) {
			
			Set<Element> persons = candidateMap.get(sid);
			
			if ( persons.size() > 1 ) {
				
				System.out.println("multiple mentions for SID " + sid + ": " + persons.size());
				
				Person known = SoldiersModel.getPerson(ConnectionManager.getConnection(), sid);
				System.out.println("Database entry: " + known + " -- " + known.getService());
				
				System.out.println("from source:");			
				for (Element p: persons ) {
					
					Person person = Soldiers.parsePerson(p);
					System.out.println(person + " -- " + person.getService());
				}
			}
		}
	}

	
	public static Map<String, Integer> getCountsBySource(Connection connection) {
		
		Map<String, Integer> counts = new HashMap<>();
		
		String sql = "select source, count(*) as \"MENTIONS\" from MENTIONS group by source";

		try {
				
			PreparedStatement stmt = connection.prepareStatement(sql);
			
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				String source = results.getString("SOURCE");
				int mentions = results.getInt("MENTIONS");
				counts.put(source, mentions);
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return counts;
	}

}
