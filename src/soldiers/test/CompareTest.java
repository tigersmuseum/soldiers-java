package soldiers.test;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
		
		Person p1 = SoldiersModel.getPerson(connection, 164530);
		Person p2 = SoldiersModel.getPerson(connection, 164532);
		
		Compare compare = new Compare(p1, p2);
		//System.out.println(compare);
		compare.makeComparison();
	}

}
