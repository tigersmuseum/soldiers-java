package soldiers.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soldiers.database.MentionsModel;
import soldiers.database.SoldiersNamespaceContext;

public class Collect {

	public static void main(String[] args) throws XPathExpressionException, SAXException, TransformerException, MalformedURLException, ParseException, FileNotFoundException {

	    XmlUtils xmlutils = new XmlUtils();
	    Map<String, Element> sourceMap = new HashMap<String, Element>();
		
		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpath = factory.newXPath();
		NamespaceContext namespaceContext = new SoldiersNamespaceContext();
		xpath.setNamespaceContext(namespaceContext);
		
		Document sources = xmlutils.parse(new File("/C:/workspaces/development/Research/data/sources.xml"));
		sources.normalize();
		
		XPathExpression sourceExpr = xpath.compile(".//source");
		NodeList list = (NodeList) sourceExpr.evaluate(sources.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int i = 0; i < list.getLength(); i++ ) {

			Element e = (Element) list.item(i);
			sourceMap.put(e.getAttribute("name"), e);
		}
		
		Set<Long> wanted = new HashSet<Long>();
		wanted.add((long) 120793);
		wanted.add((long) 170957);
		
		Connection connection = ConnectionManager.getConnection();
		
		Set<String> mentions = new HashSet<String>();
		
		// get the list of sources we need to look at ...
		// or should we just do all of them?
		
		for (long sid: wanted ) {
			
			mentions.addAll(MentionsModel.getSourcesMentioningSoldierId(connection, sid));
		}
				
		// get the database details for each wanted soldier
		
		Map<Long, Document> wantedPersonMap = new HashMap<Long, Document>();
		
		for ( long wantedSoldier: wanted ) {
			
			Document d = Report.getPersonDOM(wantedSoldier);
			wantedPersonMap.put(wantedSoldier, d);
		}

		// get wanted mentions from sources
		
		Map<Long, Set<Document>> personMentionMap = new HashMap<Long, Set<Document>>();

		for ( String mentionedSource: mentions ) {
			
			URL url = new URL(sourceMap.get(mentionedSource).getAttribute("file"));
			String srcName = sourceMap.get(mentionedSource).getAttribute("name");
			File file = new File(url.getFile());
			scanSourceFile(wanted, personMentionMap, file, srcName, xmlutils, xpath);
		}
		
		// output
		output(wantedPersonMap, personMentionMap, xmlutils);
		
	}

	
	private static void scanSourceFile(Set<Long> wanted, Map<Long, Set<Document>> personMentionMap, File file, String srcName, XmlUtils xmlutils, XPath xpath) throws XPathExpressionException {

		Document source = xmlutils.parse(file);
		XPathExpression sourceExpr = xpath.compile(".//soldiers:candidate");
		
		NodeList list = (NodeList) sourceExpr.evaluate(source.getDocumentElement(), XPathConstants.NODESET);
		
		for ( int i = 0; i < list.getLength(); i++ ) {
		
			Element e = (Element) list.item(i);
			long sid = Long.parseLong(e.getAttribute("sid"));
			
			if ( wanted.contains(sid) ) {
				
				Document mentionDoc = xmlutils.newDocument();
				Element person = (Element) mentionDoc.importNode(e.getParentNode(), true);
				person.setAttribute("source", srcName);
				mentionDoc.appendChild(person);

				Set<Document> mentions = personMentionMap.get(sid);
				if ( mentions == null )  mentions = new HashSet<Document>();
				mentions.add(mentionDoc);
				
				personMentionMap.put(sid, mentions);
			}		
		}
	}
	
	
	private static void output(Map<Long, Document> wantedPersonMap, Map<Long, Set<Document>> personMentionMap, XmlUtils xmlutils) throws FileNotFoundException, TransformerException {
		
		Document output = xmlutils.newDocument();
		Element list = output.createElement("list");
		output.appendChild(list);
		
		for ( long soldier: wantedPersonMap.keySet() ) {
			
			Element bio = output.createElement("bio");
			bio.setAttribute("sid", String.valueOf(soldier));
			
			Element database = output.createElement("database");
			Element personDB = (Element) output.importNode(wantedPersonMap.get(soldier).getDocumentElement(), true);
			database.appendChild(personDB);
			bio.appendChild(database);
			
			Element sources = output.createElement("sources");
			
			for ( Document mentionDoc: personMentionMap.get(soldier) ) {
				
				Element source = output.createElement("source");
				Element personMention = (Element) output.importNode(mentionDoc.getDocumentElement(), true);
				Attr name = personMention.getAttributeNode("source");
				source.setAttribute("name", name.getValue());
				personMention.removeAttributeNode(name);
				source.appendChild(personMention);
				sources.appendChild(source);
			}

			bio.appendChild(sources);
			list.appendChild(bio);
			
			output.normalize();
			
			XmlUtils.writeDocument(output, new FileOutputStream("output/bio.xml"));
			
		}		
	}
}
