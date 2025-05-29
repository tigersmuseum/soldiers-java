package soldiers.utilities;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NameTokens {

	public static void main(String[] args) throws SQLException, FileNotFoundException {
		
		
    	if ( args.length < 1 ) {
    		
    		System.err.println("Usage: NameTokens <output-filename>");
    		System.exit(1);
    	}
    	
    	String outputfile = args[0];
    	PrintWriter out = new PrintWriter(outputfile);

		String sql1 = "select FORENAMES from PERSON";
		String sql2 = "select SURNAME from PERSON";
		
		Set<String> nameTokens = new HashSet<String>();
		
		Connection connection = ConnectionManager.getConnection();
        Statement jdbc = connection.createStatement();
        
        jdbc.execute(sql1);
        
        ResultSet results = jdbc.getResultSet();
        
        while ( results.next() ) {
        	
        	String txt = results.getString(1);
        	if ( txt != null ) nameTokens.addAll(Arrays.asList(txt.split("\\s+")));
        }
        
        jdbc.execute(sql2);
        
        results = jdbc.getResultSet();
        
        while ( results.next() ) {
        	
        	String txt = results.getString(1);
        	if ( txt != null ) nameTokens.addAll(Arrays.asList(txt.split("\\s+")));
        }
        
        List<String> list = new ArrayList<String>();
        list.addAll(nameTokens);
        Collections.sort(list);
        
        for ( String token: list ) {
        	
        	out.println(token);
        }
        
        out.close();
	}

}
