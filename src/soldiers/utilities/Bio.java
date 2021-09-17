package soldiers.utilities;

import java.sql.Connection;

import soldiers.database.Person;
import soldiers.database.SoldiersModel;

public class Bio {

	public static void main(String[] args) {

		if ( args.length == 0 ) {
			
			System.err.println("Usage: Bio <SID>");
			System.exit(-1);
			
		}
		String sid = args[0];
		
		 Connection connection = ConnectionManager.getConnection();
		 Person person = SoldiersModel.getPerson(connection, Long.valueOf(sid));
		 
		 if ( person.getSoldierId() < 0) {
			 
			System.out.println("No record of a soldier with ID: " + sid);
			System.exit(0);
		 }
		 
		 System.out.println(person);
		 
		 // name and service details from database
		 
		 // Find (merge?) any biographical notes - find where?
		 
		 // transform results to some report format
	}

}
