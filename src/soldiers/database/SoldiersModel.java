package soldiers.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SoldiersModel {

	public  static final String XML_NAMESPACE = "http://royalhampshireregiment.org/soldiers";

	public static long getNextAvailableSoldierId(Connection connection) {
		
		long sid = -1;
		
		String sql = "select max(SID) as NXT from PERSON";

		try {
				
			PreparedStatement stmt = connection.prepareStatement(sql);
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				sid = results.getInt("NXT");
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		if ( sid > 0 ) sid++;
		return sid;
	}

	
	public static Set<Integer> getSoldierIdsForNameInitials(Connection connection, Person person) {
		
		Set<Integer> candidates = new HashSet<Integer>();
		
		String sql = "select P.SID from PERSON P, SERVICE S where P.SURNAME = ? and P.SID = S.SID and P.INITIALS = ? and S.NUM is NULL";

		try {
				
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, person.getSurname().toUpperCase());
			stmt.setString(2, person.getInitials().toUpperCase());
			
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				candidates.add(results.getInt("SID"));
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return candidates;
	}

	
	public static Set<Integer> getSoldierIdsForNumberName(Connection connection, Person person) {
		
		Set<Integer> candidates = new HashSet<Integer>();
		
		String sql = "select distinct P.SID from PERSON P, SERVICE S where P.SURNAME = ? and P.SID = S.SID and S.NUM like ?";

		try {
			
			Set<Service> service = person.getService();
			Service svc = service.iterator().next();
			
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, person.getSurname());
			stmt.setString(2, svc.getNumber());
			
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				candidates.add(results.getInt("SID"));
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return candidates;
	}

	
	public static List<Person> getCandidatesForNumberName(Connection connection, Person person) {
		
		List<Person> candidates = new ArrayList<Person>();
		
		if ( person.getService().size() == 0 ) return candidates;
		
		String sql = "select P.SID, P.SURNAME, P.INITIALS, P.FORENAMES, S.NUM, S.RANK_ABBREV, S.REGIMENT from PERSON P, SERVICE S where P.SURNAME = ? and P.SID = S.SID and S.NUM != '' and S.NUM like ?";

		Set<Service> service = person.getService();
		Iterator<Service> iter = service.iterator();
		
		while ( iter.hasNext() ) {
			
			Service svc = iter.next();

			if (  svc.getNumber().length() > 0 ) { // there must be a number ...
				
				try {
					
					PreparedStatement stmt = connection.prepareStatement(sql);
					stmt.setString(1, person.getSurname());
					stmt.setString(2, "%" + svc.getNumber());
					
					ResultSet results = stmt.executeQuery();
					
					while ( results.next() ) {
						
						Person candidate = new Person();
						Service ss = new Service();

						candidate.setSoldierId(results.getLong("SID"));
						ss.setNumber(results.getString("NUM"));
						ss.setRank(results.getString("RANK_ABBREV"));
						ss.setRegiment(results.getString("REGIMENT"));
						candidate.setSurname(results.getString("SURNAME"));
						candidate.setInitials(results.getString("INITIALS"));
						candidate.setForenames(results.getString("FORENAMES"));
						
						candidate.addService(ss);

						candidates.add(candidate);
					}
					
					stmt.close();
				}
				catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
					
		return candidates;
	}

	
	public static List<Person> getCandidatesForNameInitials(Connection connection, Person person) {
		
		List<Person> candidates = new ArrayList<Person>();
		
		String sql = "select P.SID, P.SURNAME, P.INITIALS, P.FORENAMES, S.NUM, S.RANK_ABBREV, S.REGIMENT from PERSON P, SERVICE S where P.SURNAME = ? and P.SID = S.SID and P.INITIALS like ?";

		String initials = person.getInitials() != null && person.getInitials().length() > 0 ? person.getInitials().substring(0, 1) + "%" : "%";
		
		try {
				
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, person.getSurname());
			stmt.setString(2,  initials);
			
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				Person candidate = new Person();
				Service svc = new Service();

				candidate.setSoldierId(results.getLong("SID"));
				svc.setNumber(results.getString("NUM"));
				svc.setRank(results.getString("RANK_ABBREV"));
				svc.setRegiment(results.getString("REGIMENT"));
				candidate.setSurname(results.getString("SURNAME"));
				candidate.setInitials(results.getString("INITIALS"));
				candidate.setForenames(results.getString("FORENAMES"));

				candidate.addService(svc);
				candidates.add(candidate);
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return candidates;
	}

	
	public static List<Person> getCandidatesForSurname(Connection connection, Person person) {
		
		List<Person> candidates = new ArrayList<Person>();
		
		String sql = "select P.SID, P.SURNAME, P.INITIALS, P.FORENAMES, S.NUM, S.RANK_ABBREV, S.REGIMENT from PERSON P, SERVICE S where P.SURNAME = ? and P.SID = S.SID";

		try {
				
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, person.getSurname());
			
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				Person candidate = new Person();
				Service svc = new Service();

				candidate.setSoldierId(results.getLong("SID"));
				svc.setNumber(results.getString("NUM"));
				svc.setRank(results.getString("RANK_ABBREV"));
				svc.setRegiment(results.getString("REGIMENT"));
				candidate.setSurname(results.getString("SURNAME"));
				candidate.setInitials(results.getString("INITIALS"));
				candidate.setForenames(results.getString("FORENAMES"));

				candidate.addService(svc);
				candidates.add(candidate);
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return candidates;
	}


	
	public static List<Person> getCandidatesForExactNumber(Connection connection, Person person) {
		
		List<Person> candidates = new ArrayList<Person>();
		
		String sql = "select P.SID, P.SURNAME, P.INITIALS, P.FORENAMES, S.NUM, S.RANK_ABBREV, S.REGIMENT from PERSON P, SERVICE S where P.SID = S.SID and S.NUM = ?";

		try {
				
			Set<Service> service = person.getService();
			Service svc = service.iterator().next();
			
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, svc.getNumber());
			
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				Person candidate = new Person();
				Service ss = new Service();

				candidate.setSoldierId(results.getLong("SID"));
				ss.setNumber(results.getString("NUM"));
				ss.setRank(results.getString("RANK_ABBREV"));
				ss.setRegiment(results.getString("REGIMENT"));
				candidate.setSurname(results.getString("SURNAME"));
				candidate.setInitials(results.getString("INITIALS"));
				candidate.setForenames(results.getString("FORENAMES"));

				candidate.addService(ss);
				candidates.add(candidate);
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return candidates;
	}

	
	public static List<Person> getCandidatesForNumber(Connection connection, Person person) {
		
		List<Person> candidates = new ArrayList<Person>();
		
		String sql = "select P.SID, P.SURNAME, P.INITIALS, P.FORENAMES, S.NUM, S.RANK_ABBREV, S.REGIMENT from PERSON P, SERVICE S where P.SID = S.SID and S.NUM like ?";

		try {
				
			Set<Service> service = person.getService();
			Service svc = service.iterator().next();
			
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, "%" + svc.getNumber().replace("\\d+/", ""));
			
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				Person candidate = new Person();
				Service ss = new Service();

				candidate.setSoldierId(results.getLong("SID"));
				ss.setNumber(results.getString("NUM"));
				ss.setRank(results.getString("RANK_ABBREV"));
				ss.setRegiment(results.getString("REGIMENT"));
				candidate.setSurname(results.getString("SURNAME"));
				candidate.setInitials(results.getString("INITIALS"));
				candidate.setForenames(results.getString("FORENAMES"));

				candidate.addService(ss);
				candidates.add(candidate);
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return candidates;
	}

	
	public static Person getPerson(Connection connection, long sid) {
		
		Person person = new Person();
		
		String sqlp = "select SID, SURNAME, INITIALS, FORENAMES, BIRTH, DEATH, BORNAFTER, BORNBEFORE, DIEDAFTER, DIEDBEFORE from PERSON where SID = ?";
		String sqls = "select NUM, RANK_ABBREV, RANK_QUALIFIER, REGIMENT, UNIT, AFTER, BEFORE from SERVICE where SID = ? ORDER BY AFTER ASC, BEFORE DESC";

		try {
				
			PreparedStatement stmtp = connection.prepareStatement(sqlp);
			stmtp.setLong(1, sid);
			
			ResultSet results = stmtp.executeQuery();
			
			if ( results.next() ) {
				
				person.setSoldierId(results.getLong("SID"));
				person.setSurname(results.getString("SURNAME"));
				person.setInitialsAsGiven(results.getString("INITIALS"));
				person.setForenamesOnly(results.getString("FORENAMES"));
				person.setBirth(results.getDate("BIRTH"));
				person.setDeath(results.getDate("DEATH"));
				person.setBornafter(results.getDate("BORNAFTER"));
				person.setBornbefore(results.getDate("BORNBEFORE"));
				person.setDiedafter(results.getDate("DIEDAFTER"));
				person.setDiedbefore(results.getDate("DIEDBEFORE"));
				
				PreparedStatement stmts = connection.prepareStatement(sqls);
				stmts.setLong(1, sid);
				ResultSet svcresults = stmts.executeQuery();
				
				HashSet<Service> service = new HashSet<Service>();
				
				while ( svcresults.next() ) {
					
					Service svc = new Service();
					svc.setRank(svcresults.getString("RANK_ABBREV"));
					svc.setRankqualifier(svcresults.getString("RANK_QUALIFIER"));
					svc.setNumber(svcresults.getString("NUM"));
					svc.setRegiment(svcresults.getString("REGIMENT"));
					svc.setUnit(svcresults.getString("UNIT"));
					svc.setAfter(svcresults.getDate("AFTER"));
					svc.setBefore(svcresults.getDate("BEFORE"));
					
					svc.setSoldierId(sid);
					service.add(svc);
				}
				
				person.setService(service);			
			}
			
			stmtp.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return person;
	}
	

	public static Set<String> getRanks(Connection connection) {
		
		Set<String> ranks = new HashSet<String>();
		
		String sql = "select ABBREV from RANK";

		try {
				
			PreparedStatement stmt = connection.prepareStatement(sql);

			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				ranks.add(results.getString("ABBREV"));
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return ranks;
	}
	

	public static Map<String, Integer> getRankOrdinals(Connection connection) {
		
		Map<String, Integer> ranks = new HashMap<String, Integer>();
		
		String sql = "select ABBREV, ORDINAL from RANK";

		try {
				
			PreparedStatement stmt = connection.prepareStatement(sql);

			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				ranks.put(results.getString("ABBREV"), results.getInt("ORDINAL"));
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return ranks;
	}
	

	public static void insertPeople(Connection connection, List<Person> people) {
		
		for (Person person: people) {
			
			insertPerson(connection, person);
		}
	}
	

	public static void insertPerson(Connection connection, Person person) {
		
		String personSql  = "insert into PERSON (SID, SURNAME, FORENAMES, INITIALS, BIRTH, DEATH, BORNAFTER, BORNBEFORE, DIEDAFTER, DIEDBEFORE) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		if ( person.getSoldierId() < 0 ) {
			
			System.err.println("Attempting to insert a person without a Soldier ID");
			return;
		}
		
		try {
			
			PreparedStatement personStmt  = connection.prepareStatement(personSql);
			
			personStmt.setLong(1, person.getSoldierId());
			personStmt.setString(2, person.getSurname());
			personStmt.setString(3, person.getForenames());
			personStmt.setString(4, person.getInitials());
			personStmt.setDate(5, person.getBirth());
			personStmt.setDate(6, person.getDeath());
			personStmt.setDate(7, person.getBornafter());
			personStmt.setDate(8, person.getBornbefore());
			personStmt.setDate(9, person.getDiedafter());
			personStmt.setDate(10, person.getDiedbefore());
			
			personStmt.executeUpdate();
			personStmt.close();
			
			for ( Service service: person.getService() ) {
				
				insertService(connection, service);				
			}
		}
		catch (SQLException e) {
			
			System.err.println("message: " + e.getMessage());
		}
	}
	

	public static void insertService(Connection connection, Service service) {
		
		String serviceSql = "insert into SERVICE (SID, RANK_ABBREV, NUM, REGIMENT, AFTER, BEFORE, UNIT) values(?, ?, ?, ?, ?, ?,?)";
		
		try {
			
			PreparedStatement serviceStmt = connection.prepareStatement(serviceSql);

			serviceStmt.setLong(1, service.getSoldierId());
			serviceStmt.setString(2, service.getRank());
			serviceStmt.setString(3, service.getNumber());
			serviceStmt.setString(4, service.getRegiment());
			serviceStmt.setDate(5, service.getAfter());
			serviceStmt.setDate(6, service.getBefore());
			serviceStmt.setString(7, service.getUnit());
			
			serviceStmt.executeUpdate();
			
			serviceStmt.close();
		}
		catch (SQLException e) {
			
			System.err.println("message: " + e.getMessage());
		}
	}

	
	public static Set<Person> checkIdentity(Connection connection, Person person) {
		
		Set<Person> results = new HashSet<Person>();
		Set<Integer> tids = new HashSet<Integer>();

		Set<Service> service = person.getService();
		Service svc = service.iterator().next();
		
		if ( svc.getNumber() != null && svc.getNumber().length() > 0 && person.getSurname() != null ) {
			
			results.addAll(SoldiersModel.getCandidatesForNumberName(connection, person));
		}
		else if ( person.getSurname() != null && person.getInitials() != null ) {
				
			tids.addAll(SoldiersModel.getSoldierIdsForNameInitials(connection, person));
		}
		else {
			System.out.println("can't do that yet...");
		}
		
		for ( long tid: tids ) {
			
			results.add(SoldiersModel.getPerson(connection, tid));		
		}
		
		return results;
	}
		

	public static int updateBornAfter(Connection connection, Person person) {
		
		String sql = "update PERSON set BORNAFTER = ? where SID = ?";

		int rows = 0;
		
		try {
			
			PreparedStatement updateStmt = connection.prepareStatement(sql);
			
			updateStmt.setDate(1, new  java.sql.Date(person.getBornafter().getTime()));
			updateStmt.setFloat(2, person.getSoldierId());

			rows = updateStmt.executeUpdate();
			updateStmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return rows;
	}
	

	public static int updateDiedAfter(Connection connection, Person person) {
		
		String sql = "update PERSON set DIEDAFTER = ? where SID = ?";
	
		int rows = 0;
		
		try {
			
			PreparedStatement updateStmt = connection.prepareStatement(sql);
			
			updateStmt.setDate(1, new  java.sql.Date(person.getDiedafter().getTime()));
			updateStmt.setFloat(2, person.getSoldierId());
	
			rows = updateStmt.executeUpdate();
			updateStmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return rows;
	}
	
	
	public static int updateSurname(Connection connection, Person person) {
		
		String sql = "update PERSON set SURNAME = ? where SID = ?";
	
		int rows = 0;
		
		try {
			
			PreparedStatement updateStmt = connection.prepareStatement(sql);
			
			updateStmt.setString(1, person.getSurname());
			updateStmt.setFloat(2, person.getSoldierId());
	
			rows = updateStmt.executeUpdate();
			updateStmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return rows;
	}
	

	public static int updateForenames(Connection connection, Person person) {
		
		String sql = "update PERSON set FORENAMES = ? where SID = ?";
	
		int rows = 0;
		
		try {
			
			PreparedStatement updateStmt = connection.prepareStatement(sql);
			
			updateStmt.setString(1, person.getForenames());
			updateStmt.setFloat(2, person.getSoldierId());
	
			rows = updateStmt.executeUpdate();
			updateStmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return rows;
	}
	

	public static int updateInitials(Connection connection, Person person) {
		
		String sql = "update PERSON set INITIALS = ? where SID = ?";
	
		int rows = 0;
		
		try {
			
			PreparedStatement updateStmt = connection.prepareStatement(sql);
			
			updateStmt.setString(1, person.getInitials());
			updateStmt.setFloat(2, person.getSoldierId());
	
			rows = updateStmt.executeUpdate();
			updateStmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return rows;
	}
	

	public static int updateBirth(Connection connection, Person person) {
		
		String sql = "update PERSON set BIRTH = ? where SID = ?";
	
		int rows = 0;
		
		try {
			
			PreparedStatement updateStmt = connection.prepareStatement(sql);
			
			updateStmt.setDate(1, new  java.sql.Date(person.getBirth().getTime()));
			updateStmt.setFloat(2, person.getSoldierId());
	
			rows = updateStmt.executeUpdate();
			updateStmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return rows;
	}
	

	public static int updateDeath(Connection connection, Person person) {
		
		String sql = "update PERSON set DEATH = ? where SID = ?";
	
		int rows = 0;
		
		try {
			
			PreparedStatement updateStmt = connection.prepareStatement(sql);
			
			updateStmt.setDate(1, new  java.sql.Date(person.getDeath().getTime()));
			updateStmt.setFloat(2, person.getSoldierId());
	
			rows = updateStmt.executeUpdate();
			updateStmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return rows;
	}


	public static int updateBornBefore(Connection connection, Person person) {
		
		String sql = "update PERSON set BORNBEFORE = ? where SID = ?";
	
		int rows = 0;
		
		try {
			
			PreparedStatement updateStmt = connection.prepareStatement(sql);
			
			updateStmt.setDate(1, new  java.sql.Date(person.getBornbefore().getTime()));
			updateStmt.setFloat(2, person.getSoldierId());
	
			rows = updateStmt.executeUpdate();
			updateStmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return rows;
	}


	public static int updateDiedBefore(Connection connection, Person person) {
		
		String sql = "update PERSON set DIEDBEFORE = ? where SID = ?";
	
		int rows = 0;
		
		try {
			
			PreparedStatement updateStmt = connection.prepareStatement(sql);
			
			updateStmt.setDate(1, new  java.sql.Date(person.getDiedbefore().getTime()));
			updateStmt.setFloat(2, person.getSoldierId());
	
			rows = updateStmt.executeUpdate();
			updateStmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return rows;
	}
	
	
	public static int updateUnit(Connection connection, Service service) {
		
		String sql = "update SERVICE set UNIT = ? where SID = ? and NUM = ? and RANK_ABBREV = ? and REGIMENT = ?";
	
		int rows = 0;
		
		try {
			
			PreparedStatement updateStmt = connection.prepareStatement(sql);
			
			updateStmt.setString(1, service.getUnit());
			updateStmt.setFloat(2, service.getSoldierId());
			updateStmt.setString(3, service.getNumber());
			updateStmt.setString(4, service.getRank());
			updateStmt.setString(5, service.getRegiment());
	
			rows = updateStmt.executeUpdate();
			updateStmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return rows;
	}

	
	public static int updateServiceBefore(Connection connection, Service service, Service prior) {
		
		String sql = "update SERVICE set BEFORE = ? where SID = ? and NUM = ? and RANK_ABBREV = ? and REGIMENT = ? and BEFORE = ?";
	
		int rows = 0;
		
		try {
			
			PreparedStatement updateStmt = connection.prepareStatement(sql);
			
			updateStmt.setDate(1, service.getBefore());
			updateStmt.setFloat(2, service.getSoldierId());
			updateStmt.setString(3, service.getNumber());
			updateStmt.setString(4, service.getRank());
			updateStmt.setString(5, service.getRegiment());
			updateStmt.setDate(6, prior.getBefore());
	
			rows = updateStmt.executeUpdate();
			updateStmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return rows;
	}

	
	public static int updateServiceAfter(Connection connection, Service service, Service prior) {
		
		String sql = "update SERVICE set AFTER = ? where SID = ? and NUM = ? and RANK_ABBREV = ? and REGIMENT = ? and BEFORE = ?";
	
		int rows = 0;
		
		try {
			
			PreparedStatement updateStmt = connection.prepareStatement(sql);
			
			updateStmt.setDate(1, service.getAfter());
			updateStmt.setFloat(2, service.getSoldierId());
			updateStmt.setString(3, service.getNumber());
			updateStmt.setString(4, service.getRank());
			updateStmt.setString(5, service.getRegiment());
			updateStmt.setDate(6, prior.getBefore());
	
			rows = updateStmt.executeUpdate();
			updateStmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return rows;
	}
}
