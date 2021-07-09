package soldiers.utilities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    public static Connection getDerbyConnection() {
		
		Connection connection = null;
		
 		try {
			String dburl = "jdbc:derby:J:/Soldiers/TIGERS";
			connection = DriverManager.getConnection(dburl, "user", "password");
		}
 		catch (SQLException e) {

			e.printStackTrace();
		}
 		
 		return connection;
	}

    public static Connection getAccessConnection() {
		
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
