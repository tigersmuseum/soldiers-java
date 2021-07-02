package soldiers.database;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class TigersNamespaceContext implements NamespaceContext {

	@Override
	public String getNamespaceURI(String prefix) {

        if (prefix == null) throw new NullPointerException("Null prefix");
        else if ("xhtml".equals(prefix)) return "http://www.w3.org/1999/xhtml";
        else if ("soldiers".equals(prefix)) return SoldiersModel.XML_NAMESPACE;
        else if ("xml".equals(prefix)) return XMLConstants.XML_NS_URI;
        return XMLConstants.NULL_NS_URI;
	}

	@Override
	public String getPrefix(String uri) {

		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<String> getPrefixes(String uri) {

		throw new UnsupportedOperationException();
	}

}
