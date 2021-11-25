package soldiers.utilities;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.w3c.dom.Document;

public class HtmlUtils {


	public HtmlUtils() {

	}
		
	
	public Document cleanFile(File file, String encoding) {
		
		W3CDom  w3cdom = new W3CDom();		
		org.jsoup.nodes.Document doc = null;

		try {
			doc = Jsoup.parse(file, encoding);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return w3cdom.fromJsoup(doc);
	}

}
