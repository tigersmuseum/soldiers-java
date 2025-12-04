package soldiers.utilities;

import java.sql.Date;
import java.util.Comparator;

import soldiers.database.Service;

public class ServiceComparator implements Comparator<Service> {

	public int compare(Service s1, Service s2) {
		Date s1date = s1.getBefore();
		Date s2date = s2.getBefore();
		
		if (s1date != null && s2date != null ) {
			
			return s1.getBefore().compareTo(s2.getBefore());
		}
		else return 0;
	}

}
