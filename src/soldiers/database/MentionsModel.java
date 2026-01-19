package soldiers.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class MentionsModel {

	public static Set<String> getSources(Connection connection) {
		
		String sql = "select distinct SOURCE from MENTIONS";
		
		Set<String> sources = new HashSet<String>();

		try {
				
			PreparedStatement stmt = connection.prepareStatement(sql);
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				sources.add(results.getString("SOURCE"));
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return sources;
	}	

	public static Set<String> getSourcesMentioningSoldierId(Connection connection, long sid) {
		
		String sql = "select * from MENTIONS where SID = ?";
		
		Set<String> sources = new HashSet<String>();

		try {
				
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setLong(1, sid);
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				sources.add(results.getString("SOURCE"));
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return sources;
	}	

	
	public static void insertMention(Connection connection, long sid, String source) {
		
		String serviceSql = "insert into MENTIONS (SID, SOURCE) values(?, ?)";
		
		try {
			
			PreparedStatement serviceStmt = connection.prepareStatement(serviceSql);

			serviceStmt.setLong(1, sid);
			serviceStmt.setString(2, source);
			
			serviceStmt.executeUpdate();
			
			serviceStmt.close();
		}
		catch (SQLException e) {
			
			System.err.println("message: " + e.getMessage());
		}
	}
	
}
