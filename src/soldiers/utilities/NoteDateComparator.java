package soldiers.utilities;

import java.util.Comparator;

import org.w3c.dom.Element;

public class NoteDateComparator implements Comparator<Note> {

	@Override
	public int compare(Note noteA, Note noteB) {

		Element elementA = noteA.getElement();
		Element elementB = noteB.getElement();
		
		String dateA = elementA.getAttribute("date");
		String dateB = elementB.getAttribute("date");
		
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
