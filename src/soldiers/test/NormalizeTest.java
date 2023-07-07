package soldiers.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import soldiers.database.Normalize;

class NormalizeTest {

	@Test
	void ranks() {
		fail("Not yet implemented");
	}

	@Test
	void dates() {
		
		assert(Normalize.normalizeDate("1899-04-18").equals("1899-04-18"));
		assert(Normalize.normalizeDate("Mar-1946").equals("1946-03"));
		assert(Normalize.normalizeDate("18.4.1899").equals("1899-04-18"));
		assert(Normalize.normalizeDate("18/4/1899").equals("1899-04-18"));
		assert(Normalize.normalizeDate("1900").equals("1900"));
		assert(Normalize.normalizeDate("7.1976").equals("1976-07"));

		assert(Normalize.after("1914").equals("1914-01-01"));
		assert(Normalize.before("1914").equals("1914-12-31"));
		
		assert(Normalize.after("1914-02").equals("1914-02-01"));
		assert(Normalize.before("1914-02").equals("1914-02-28"));
		
		assert(Normalize.after("1916-02").equals("1916-02-01"));		
		assert(Normalize.before("1916-02").equals("1916-02-29"));	// leap year	

		assertNull(Normalize.before("12345"));
	}

}
