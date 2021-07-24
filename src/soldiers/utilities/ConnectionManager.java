package soldiers.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    public static Connection getConnection() {
		
    	// String dburl = "jdbc:derby:J:/Soldiers/TIGERS";
    	// jdbc:ucanaccess://H:/Archive/Admin/Database/TIGERS.accdb
    	
		Connection connection = null;
		
		String dburl = System.getProperty("soldiers.database.url");
		
		if ( dburl == null ) {
			
			System.err.println("Set the JDBC URL using system property: soldiers.database.url");		
		}
		else if ( dburl.startsWith("jdbc:ucanaccess:") ) {
			
			connection = getAccessConnection(dburl);
		}
		else if ( dburl.startsWith("jdbc:derby:") ) {
			
			connection = getDerbyConnection(dburl);
		}
		else {
			
			System.err.println("JDBC URL: " + dburl);					
			System.err.println("Expecting URL to start \"jdbc:ucanaccess:\" or \"jdbc:derby:\"");					
		}
		
 		return connection;
	}

    public static Connection getDerbyConnection(String dburl) {
		
		Connection connection = null;
		
 		try {
			connection = DriverManager.getConnection(dburl, "user", "password");
		}
 		catch (SQLException e) {

			e.printStackTrace();
		}
 		
 		return connection;
	}

    public static Connection getAccessConnection(String dburl) {
		
		Connection connection = null;
		
 		try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");//Loading Driver
            connection= DriverManager.getConnection("jdbc:ucanaccess://H:/Archive/Admin/Database/TIGERS.accdb");
		}
 		catch (SQLException e) {

			e.printStackTrace();
		}
 		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
 		
 		return connection;
	}

}
