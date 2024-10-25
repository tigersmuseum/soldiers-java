package soldiers.utilities;

import java.util.Comparator;

import soldiers.database.Service;

public class ServiceComparator implements Comparator<Service> {

	public int compare(Service s1, Service s2) {
		return s1.getBefore().compareTo(s2.getBefore());
	}

}
