package soldiers.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;

public class HtmlUtils {

	private CleanerProperties props;
	private HtmlCleaner cleaner;
	private DomSerializer serializer;

	public HtmlUtils() {

		props = new CleanerProperties();
		props.setNamespacesAware(false);
		props.setOmitDoctypeDeclaration(true);
		props.setRecognizeUnicodeChars(true);
		props.setTranslateSpecialEntities(false);
		props.setAdvancedXmlEscape(false);

		cleaner = new HtmlCleaner(props);
		serializer = new DomSerializer(props);
	}
	
	
	public Document cleanFile(File file) {
		
		Document doc = null;
		
		try {
			TagNode tn = cleaner.clean(new FileInputStream(file), "UTF-8");
			doc = serializer.createDOM(tn);			
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		return doc;
	}

}
