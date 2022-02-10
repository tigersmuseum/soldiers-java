package soldiers.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.codec.language.Metaphone;
//import org.apache.commons.codec.language.RefinedSoundex;
// org.apache.commons.codec.language.Soundex;

import soldiers.utilities.ConnectionManager;

public class MakeEncoderMap {

	public static void main(String[] args) {

		Connection connection = ConnectionManager.getConnection();
		Metaphone encoder = new Metaphone();
		
		Map<String, List<String>> soundMap = getEncoderMap(encoder, connection);

		
		String sound = encoder.encode("WAR");
		
		List<String> names = soundMap.get(sound);
		
		for ( String name: names ) {
			
			System.out.println(name);
		}
		
		System.out.println();
	}

	
	public static Map<String, List<String>> getEncoderMap(StringEncoder encoder, Connection connection) {

		Map<String, List<String>> soundMap = new HashMap<String, List<String>>();
		
		String sql = "select distinct(SURNAME) from PERSON";
		
		try {
			
			PreparedStatement stmt = connection.prepareStatement(sql);
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				String surname = results.getString("SURNAME");
				String sound   = encoder.encode(surname);
				
				List<String> list = soundMap.get(sound);
				
				if ( list == null ) {
					
					list = new ArrayList<String>();
				}

				list.add(surname);
				soundMap.put(sound, list);
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		catch (EncoderException e) {
			e.printStackTrace();
		}

		return soundMap;
	}

}
