package soldiers.utilities;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Sources {

	private File sourcesFile;
	private Map<String, Set<File>> sourceMap;
	private XPath xpath;
	private XmlUtils xmlutils;
	private XPathExpression sourceExpr;
	private String expr = ".//source";
	private Document sources;
	
	public Sources() {

		xmlutils = new XmlUtils();
	    xpath = xmlutils.newXPath();

		try {
			sourceExpr = xpath.compile(expr);
		}
		catch (XPathExpressionException e) {
			
			System.err.println("Invalid XPath expression: " + expr);
			System.exit(1);
		}

		sourceMap = new HashMap<String, Set<File>>();
	}

	public Sources(File sourcesFile) {
		
		this();
		setSourcesFile(sourcesFile);
	}

	public void setSourcesFile(File sourcesFile) {

		this.sourcesFile = sourcesFile;
		parse();
	}
	
	private void parse() {
		
	    xmlutils = new XmlUtils();
		sources = xmlutils.parse(sourcesFile);
		sources.normalize();
		
		try {
			NodeList list = (NodeList) sourceExpr.evaluate(sources.getDocumentElement(), XPathConstants.NODESET);

			for ( int i = 0; i < list.getLength(); i++ ) {

				Element e = (Element) list.item(i);
				String srcName = e.getAttribute("name");
				String urlString = e.getAttribute("file");
				
				try {
					URL url = new URL(urlString);
					File file = new File(url.getFile());
					
					Set<File> files = sourceMap.get(srcName);
					if ( files == null )  files = new HashSet<File>();
					files.add(file);
					sourceMap.put(srcName, files);
				}
				catch (MalformedURLException e1) {
					
					System.err.println("Invalid URL: " + urlString);
				}
				
			}
		}
		catch (XPathExpressionException e) {
			
			System.err.println("Invalid XPath expression: " + expr);
			System.exit(1);
		}
	}

	public Map<String, Set<File>> getSourceMap() {
		
		return sourceMap;
	}
		
}
