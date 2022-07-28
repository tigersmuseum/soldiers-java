package soldiers.utilities;

import java.util.Comparator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;

public class NoteDateComparator implements Comparator<Note> {

	private XPathFactory factory = XPathFactory.newInstance();
    private XPath xpath = factory.newXPath();
	private XPathExpression expr;

    
	public NoteDateComparator() {
		super();
		
		try {
			expr = xpath.compile(".//*[@date][last()]");
		}
		catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}


	@Override
	public int compare(Note noteA, Note noteB) {

		Element elementA = noteA.getElement();
		Element elementB = noteB.getElement();
		
		String dateA = getDateAttribute(elementA);
		String dateB = getDateAttribute(elementB);
		
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

	
	private String getDateAttribute(Element element) {
		
		String date = null;
		
		date = element.getAttribute("date");
		
		// if we don't have a @date attribute on the <note> element, then get the last @date attribute from the children of the <note>
		
		if ( date == "" ) {
			
			try {
				Element nested = (Element) expr.evaluate(element, XPathConstants.NODE);
				if ( nested != null ) date = nested.getAttribute("date");
			}
			catch (XPathExpressionException e) {
				e.printStackTrace();
			}

		}
		
		return date;
	}
}
