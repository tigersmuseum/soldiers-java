package soldiers.utilities;

import java.util.Comparator;

import soldiers.database.Person;

public class PersonComparator implements Comparator<Person> {

	public int compare(Person p1, Person p2) {
		return p1.getSort().compareTo(p2.getSort());
	}

}
