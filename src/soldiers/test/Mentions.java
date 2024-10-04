package soldiers.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import soldiers.database.Person;
import soldiers.database.SoldiersModel;
import soldiers.utilities.CandidateDetails;
import soldiers.utilities.ConnectionManager;
import soldiers.utilities.Soldiers;

public class Mentions {

	public static void main(String[] args) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException, ParseException {

    	if ( args.length < 2 ) {
    		
    		System.err.println("Usage: Mentions <input-filename> <output-filename>");
    		System.exit(1);
    	}

		String inputfile = args[0]; String outputfile = args[1];

		Document doc = Soldiers.readDocument(new FileInputStream(inputfile));			 
		doc.normalize();

		Map<Long, Set<Element>> lookup = CandidateDetails.getSoldierMap(doc);
		
		for ( long sid: lookup.keySet()) {
			
			Set<Element> persons = lookup.get(sid);
			
			if ( persons.size() > 1 ) {
				
				System.out.println(sid + " = " + persons.size());
				
				Person known = SoldiersModel.getPerson(ConnectionManager.getConnection(), sid);
				System.out.println(known + " -- " + known.getService());
							
				for (Element p: persons ) {
					
					Person person = Soldiers.parsePerson(p);
					System.out.println(person + " -- " + person.getService());
				}
			}
		}
		
		PrintWriter output = new PrintWriter(outputfile);

		for (Long sid: lookup.keySet() ) {
			
			output.println("SAVINGS," + sid + ",");
		}
		
		output.close();
	}

}
