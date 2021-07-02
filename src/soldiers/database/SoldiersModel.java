package soldiers.database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SoldiersModel {

	public  static final String XML_NAMESPACE = "http://royalhampshireregiment.org/soldiers";

	public static long getTigerIdForSourceItem(Connection connection, String source, int sid) {
		
		long tid = -1;
		
		String sql = "select X.SID from MAPPING as X, LONGLIST as L where L.SOURCE = ? and L.SID = ? and X.LID = L.LID";

		try {
				
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, source);
			stmt.setInt(2, sid);
			
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				tid = results.getInt("SID");
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return tid;
	}

	
	public static Set<Integer> getTigerIdsForNameInitials(Connection connection, Person person) {
		
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

	
	public static Set<Integer> getTigerIdsForNumberName(Connection connection, Person person) {
		
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

	
	public static Set<Person> getCandidatesForNumberName(Connection connection, Person person) {
		
		Set<Person> candidates = new HashSet<Person>();
		
		if ( person.getService().size() == 0 ) return candidates;
		
		String sql = "select P.SID, P.SURNAME, P.INITIALS, P.FORENAMES, S.NUM, S.RANK_ABBREV from PERSON P, SERVICE S where P.SURNAME = ? and P.SID = S.SID and S.NUM like ?";

		try {
				
			Set<Service> service = person.getService();
			Service svc = service.iterator().next();
			
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, person.getSurname());
			stmt.setString(2, "%" + svc.getNumber());
			
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				Person candidate = new Person();
				Service ss = new Service();

				candidate.setTigerId(results.getLong("SID"));
				ss.setNumber(results.getString("NUM"));
				ss.setRank(results.getString("RANK_ABBREV"));
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

	
	public static Set<Person> getCandidatesForNameInitials(Connection connection, Person person) {
		
		Set<Person> candidates = new HashSet<Person>();
		
		String sql = "select P.SID, P.SURNAME, P.INITIALS, P.FORENAMES, S.NUM, S.RANK_ABBREV from PERSON P, SERVICE S where P.SURNAME = ? and P.SID = S.SID and P.INITIALS like ?";

		String initials = person.getInitials() != null && person.getInitials().length() > 0 ? person.getInitials().substring(0, 1) + "%" : "%";
		
		try {
				
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, person.getSurname());
			stmt.setString(2,  initials);
			
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				Person candidate = new Person();
				Service svc = new Service();

				candidate.setTigerId(results.getLong("SID"));
				svc.setNumber(results.getString("NUM"));
				svc.setRank(results.getString("RANK_ABBREV"));
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

	
	public static Set<Person> getCandidatesForExactNumber(Connection connection, Person person) {
		
		Set<Person> candidates = new HashSet<Person>();
		
		String sql = "select P.SID, P.SURNAME, P.INITIALS, P.FORENAMES, S.NUM, S.RANK_ABBREV from PERSON P, SERVICE S where P.SID = S.SID and S.NUM = ?";

		try {
				
			Set<Service> service = person.getService();
			Service svc = service.iterator().next();
			
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, svc.getNumber());
			
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				Person candidate = new Person();
				Service ss = new Service();

				candidate.setTigerId(results.getLong("SID"));
				ss.setNumber(results.getString("NUM"));
				ss.setRank(results.getString("RANK_ABBREV"));
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

	
	public static Set<Person> getCandidatesForNumberNameLonglist(Connection connection, Person person) {
		
		Set<Person> candidates = new HashSet<Person>();
		
		String sql = "select LID, SURNAME, INITIALS, FORENAMES, NUM, RANK from LONGLIST where SURNAME = ? and NUM like ?";

		try {
				
			Set<Service> service = person.getService();
			Service svc = service.iterator().next();
			
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, person.getSurname());
			stmt.setString(2, "%" + svc.getNumber());
			
			ResultSet results = stmt.executeQuery();
			
			while ( results.next() ) {
				
				Person candidate = new Person();
				Service ss = new Service();

				candidate.setTigerId(results.getLong("LID"));
				ss.setNumber(results.getString("NUM"));
				ss.setRank(results.getString("RANK"));
				candidate.setSurname(results.getString("SURNAME"));
				candidate.setInitials(results.getString("INITIALS"));
				candidate.setForenames(results.getString("FORENAMES"));
				
				person.addService(ss);

				candidates.add(candidate);
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
				
		return candidates;
	}

	
	
	public static Person getPerson(Connection connection, long tid) {
		
		Person person = new Person();
		
		String sqlp = "select SID, SURNAME, INITIALS, FORENAMES, BIRTH, DEATH, BORNAFTER, BORNBEFORE, DIEDAFTER, DIEDBEFORE from PERSON where SID = ?";
		String sqls = "select NUM, RANK_ABBREV, RANK_QUALIFIER, REGIMENT, UNIT, AFTER, BEFORE from SERVICE where SID = ? ORDER BY AFTER ASC, BEFORE DESC";

		try {
				
			PreparedStatement stmtp = connection.prepareStatement(sqlp);
			stmtp.setLong(1, tid);
			
			ResultSet results = stmtp.executeQuery();
			
			if ( results.next() ) {
				
				person.setTigerId(results.getLong("SID"));
				person.setSurname(results.getString("SURNAME"));
				person.setInitials(results.getString("INITIALS"));
				person.setForenames(results.getString("FORENAMES"));
				person.setBirth(results.getDate("BIRTH"));
				person.setDeath(results.getDate("DEATH"));
				person.setBornafter(results.getDate("BORNAFTER"));
				person.setBornbefore(results.getDate("BORNBEFORE"));
				person.setDiedafter(results.getDate("DIEDAFTER"));
				person.setDiedbefore(results.getDate("DIEDBEFORE"));
				
				PreparedStatement stmts = connection.prepareStatement(sqls);
				stmts.setLong(1, tid);
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
	

	public static Person getPersonX(Connection connection, long tid) {
		
		Person person = new Person();
		
		String sql = "select P.SID, S.NUM, S.RANK_ABBREV, P.SURNAME, P.INITIALS, P.FORENAMES, P.BIRTH, P.DEATH, P.BORNAFTER, P.BORNBEFORE, P.DIEDAFTER, P.DIEDBEFORE from PERSON P, SERVICE S where P.SID = ? and P.SID = S.SID";


		try {
				
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setLong(1, tid);
			
			ResultSet results = stmt.executeQuery();
			
			if ( results.next() ) {
				
				Service svc = new Service();
				
				person.setTigerId(results.getLong("SID"));
				svc.setNumber(results.getString("NUM"));
				svc.setRank(results.getString("RANK_ABBREV"));
				person.setSurname(results.getString("SURNAME"));
				person.setInitials(results.getString("INITIALS"));
				person.setForenames(results.getString("FORENAMES"));
				person.setBirth(results.getDate("BIRTH"));
				person.setDeath(results.getDate("DEATH"));
				person.setBornafter(results.getDate("BORNAFTER"));
				person.setBornbefore(results.getDate("BORNBEFORE"));
				person.setDiedafter(results.getDate("DIEDAFTER"));
				person.setDiedbefore(results.getDate("DIEDBEFORE"));
				
				person.addService(svc);
			}
			
			stmt.close();
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
	

	public static void insertPeople(Connection connection, List<Person> people) {
		
		String personSql  = "insert into PERSON (SID, SURNAME, FORENAMES, INITIALS) values(?, ?, ?, ?)";
		String serviceSql = "insert into SERVICE (SID, RANK_ABBREV, REGIMENT, AFTER, NUMBER) values(?, ?, ?, ?, ?)";
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1900, 1, 1);
		Date after = new Date(calendar.getTimeInMillis());

		try {
			
			PreparedStatement personStmt  = connection.prepareStatement(personSql);
			PreparedStatement serviceStmt = connection.prepareStatement(serviceSql);
			
			for (Person person: people) {
				
				Set<Service> service = person.getService();
				Service svc = service.iterator().next();
				
				personStmt.setLong(1, person.getTigerId());
				personStmt.setString(2, person.getSurname());
				personStmt.setString(3, person.getForenames());
				personStmt.setString(4, person.getInitials());
				
				personStmt.executeUpdate();
				
				serviceStmt.setLong(1, person.getTigerId());
				serviceStmt.setString(2, svc.getRank());
				serviceStmt.setString(3, "Hampshire Regiment");
				serviceStmt.setDate(4, after);
				serviceStmt.setString(5, svc.getNumber());
				
				serviceStmt.executeUpdate();

			}
			
			personStmt.close();
			serviceStmt.close();
		}
		catch (SQLException e) {
			
			System.err.println("message: " + e.getMessage());
		}
	}
	

	public static void insertPerson(Connection connection, Person person) {
		
		String personSql  = "insert into PERSON (SID, SURNAME, FORENAMES, INITIALS) values(?, ?, ?, ?)";
		String serviceSql = "insert into SERVICE (SID, RANK_ABBREV, REGIMENT, AFTER, NUMBER) values(?, ?, ?, ?, ?)";
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1900, 1, 1);
		Date after = new Date(calendar.getTimeInMillis());

		try {
			
			Set<Service> service = person.getService();
			Service svc = service.iterator().next();
			
			PreparedStatement personStmt  = connection.prepareStatement(personSql);
			PreparedStatement serviceStmt = connection.prepareStatement(serviceSql);
			
			personStmt.setLong(1, person.getTigerId());
			personStmt.setString(2, person.getSurname());
			personStmt.setString(3, person.getForenames());
			personStmt.setString(4, person.getInitials());
			
			personStmt.executeUpdate();
			
			serviceStmt.setLong(1, person.getTigerId());
			serviceStmt.setString(2, svc.getRank());
			serviceStmt.setString(3, "Hampshire Regiment");
			serviceStmt.setDate(4, after);
			serviceStmt.setString(5, svc.getNumber());
			
			serviceStmt.executeUpdate();
			
			personStmt.close();
			serviceStmt.close();
		}
		catch (SQLException e) {
			
			System.err.println("message: " + e.getMessage());
		}
	}

	
	public static Person getPersonFromLonglist(Connection connection, long lid) {
		
		Person person = new Person();
		Service svc = new Service();
		
		String sql = "select M.SID, L.NUM, L.RANK, L.SURNAME, L.INITIALS,  L.FORENAMES, L.BIRTH, L.DEATH from LONGLIST L LEFT OUTER JOIN MAPPING M ON L.LID = M.LID where L.LID = ?";


		try {
				
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setLong(1, lid);
			
			ResultSet results = stmt.executeQuery();
			
			if ( results.next() ) {
				
				person.setTigerId(results.getLong("SID"));
				svc.setNumber(results.getString("NUM"));
				svc.setRank(results.getString("RANK"));
				person.setSurname(results.getString("SURNAME"));
				person.setInitials(results.getString("INITIALS"));
				person.setForenames(results.getString("FORENAMES"));
				
				person.addService(svc);
			}
			
			stmt.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

		return person;
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
				
			tids.addAll(SoldiersModel.getTigerIdsForNameInitials(connection, person));
		}
		else {
			System.out.println("can't do that yet...");
		}
		
		for ( long tid: tids ) {
			
			results.add(SoldiersModel.getPerson(connection, tid));		
		}
		
		return results;
	}
		
	public static Set<Person> checkIdentityLong(Connection connection, Person person) {
		
		Set<Person> results = new HashSet<Person>();
		Set<Integer> tids = new HashSet<Integer>();

		Set<Service> service = person.getService();
		Service svc = service.iterator().next();
		
		if ( svc.getNumber() != null && svc.getNumber().length() > 0 && person.getSurname() != null ) {
			
			results.addAll(SoldiersModel.getCandidatesForNumberNameLonglist(connection, person));
		}
		else if ( person.getSurname() != null && person.getInitials() != null ) {
				
			tids.addAll(SoldiersModel.getTigerIdsForNameInitials(connection, person));
		}
		else {
			System.out.println("can't do that yet...");
		}
		
		for ( long tid: tids ) {
			
			results.add(SoldiersModel.getPerson(connection, tid));		
		}
		
		System.out.println("......" + results.size());
		
		Iterator<Person> x = results.iterator();
		
		while ( x.hasNext() ) {
			
			System.out.println("db: " + x.next());
		}
		
		return results;
	}
		

}
