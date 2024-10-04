package soldiers.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import soldiers.database.MentionsModel;

public class Collect {

	public static void main(String[] args) throws XPathExpressionException, SAXException, TransformerException, ParseException, ParserConfigurationException, IOException {

    	if ( args.length < 3 ) {
    		
    		System.err.println("Usage: Collect <sources-filename> <input-filename> <output-filename>");
    		System.exit(1);
    	}
    	
    	String sourcesfile = args[0];
    	String inputfile   = args[1];
    	String outputfile  = args[2];
    	
		System.out.printf("sources file:  %s\n", sourcesfile);
		System.out.printf("input file:  %s\n", inputfile);
		System.out.printf("output file: %s\n\n", outputfile);
		
		Set<Long> wanted = getWanted(inputfile);		
		Sources sources = new Sources(new File(sourcesfile));
    			
		Connection connection = ConnectionManager.getConnection();
		XmlUtils xmlutils = new XmlUtils();
		
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
		Map<String, Set<File>> sourceMap = sources.getSourceMap();

		for ( String mentionedSource: mentions ) {
			
			Set<File> files = sourceMap.get(mentionedSource);
			
			for (File file: files ) {
				
				scanSourceFile(wanted, personMentionMap, file, mentionedSource, xmlutils);
			}
		}
		
		// output
		output(wantedPersonMap, personMentionMap, xmlutils, outputfile);
		
	}

	
	private static void scanSourceFile(Set<Long> wanted, Map<Long, Set<Document>> personMentionMap, File file, String srcName, XmlUtils xmlutils) throws XPathExpressionException {

		Document source = xmlutils.parse(file);
		XPath xpath = XmlUtils.newXPath();
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
	
	
	private static void output(Map<Long, Document> wantedPersonMap, Map<Long, Set<Document>> personMentionMap, XmlUtils xmlutils, String outputfile) throws FileNotFoundException, TransformerException {
		
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
			
			Set<Document> sourcefiles = personMentionMap.get(soldier);
			
			if ( sourcefiles != null ) {
				
				for ( Document mentionDoc: sourcefiles ) {
					
					Element source = output.createElement("source");
					Element personMention = (Element) output.importNode(mentionDoc.getDocumentElement(), true);
					Attr name = personMention.getAttributeNode("source");
					source.setAttribute("name", name.getValue());
					personMention.removeAttributeNode(name);
					source.appendChild(personMention);
					sources.appendChild(source);
				}
			}
			else {
				System.err.println("No source data for SID: " + soldier);
			}
			
			bio.appendChild(sources);
			list.appendChild(bio);
			
			output.normalize();
			
			XmlUtils.writeDocument(output, new FileOutputStream(outputfile));			
		}		
	}
	
	private static Set<Long> getWanted(String inputfile) throws FileNotFoundException, ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
		
		Document doc = Soldiers.readDocument(new FileInputStream(inputfile));			 
		doc.normalize();
		
		return CandidateDetails.getSoldierSet(doc);
	}
}
