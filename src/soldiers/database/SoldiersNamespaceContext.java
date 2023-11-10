package soldiers.database;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class SoldiersNamespaceContext implements NamespaceContext {

	public String getNamespaceURI(String prefix) {

        if (prefix == null) throw new NullPointerException("Null prefix");
        else if ("xhtml".equals(prefix)) return "http://www.w3.org/1999/xhtml";
        else if ("soldiers".equals(prefix)) return SoldiersModel.XML_NAMESPACE;
        else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
        return XMLConstants.NULL_NS_URI;
	}

	public String getPrefix(String uri) {

		throw new UnsupportedOperationException();
	}

	public Iterator<String> getPrefixes(String uri) {

		throw new UnsupportedOperationException();
	}

}
