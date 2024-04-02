package soldiers.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MentionsModel {

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

}
