package soldiers.test;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import soldiers.database.MentionsModel;
import soldiers.database.Person;
import soldiers.database.SoldiersModel;
import soldiers.utilities.Compare;
import soldiers.utilities.ConnectionManager;



class CompareTest {

	private static Connection connection;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		
		connection = ConnectionManager.getConnection();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		
		connection.close();
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
		
	}

	@Test
	void test() {
		
		Person p1 = SoldiersModel.getPerson(connection, 182658);
		Person p2 = SoldiersModel.getPerson(connection, 182649);
		Set<String> mentions1 = MentionsModel.getSourcesMentioningSoldierId(connection, p1.getSoldierId());
		Set<String> mentions2 = MentionsModel.getSourcesMentioningSoldierId(connection, p2.getSoldierId());

		Compare compare = new Compare(p1, p2);
		System.out.println(compare);
		compare.makeComparison();
		
		System.out.println(mentions1);
		System.out.println(mentions2);
	}

}
