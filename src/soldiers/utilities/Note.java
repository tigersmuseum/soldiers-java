package soldiers.utilities;

import org.w3c.dom.Element;

public class Note {

	private Element element;
	private int hash;
	
	public Note(Element element) {
		
		this.element = element;
		this.element.normalize();
		hash = this.element.getTextContent().hashCode();		
	}
	
	public Element getElement() {
		
		return element;
	}

	@Override
	public int hashCode() {
		
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		
		return obj.hashCode() == this.hashCode();
	}

}
