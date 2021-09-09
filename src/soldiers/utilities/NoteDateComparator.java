package soldiers.utilities;

import java.util.Comparator;

import org.w3c.dom.Element;

public class NoteDateComparator implements Comparator<Element> {

	@Override
	public int compare(Element arg0, Element arg1) {

		String dateA = arg0.getAttribute("date");
		String dateB = arg1.getAttribute("date");
		
		if ( dateA == null && dateB == null ) {		

			return 0;			
		}
		else if ( dateA == null ) {
			
			return 1;
		}
		else if ( dateB == null ) {
			
			return -1;
		}
		else {
			return dateA.compareTo(dateB);
		}
	}

}
