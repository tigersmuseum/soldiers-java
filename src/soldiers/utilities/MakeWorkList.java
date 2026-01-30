package soldiers.utilities;

import java.io.File;
import java.net.MalformedURLException;

public class MakeWorkList {

	public static void main(String[] args) throws MalformedURLException {

    	if ( args.length < 1 ) {
    		
    		System.err.println("Usage: MakeWorkList <directory>");
    		System.exit(1);
    	}
    	
    	String dirname = args[0];
    	
    	File dir = new File(dirname);
    	
    	if ( !dir.isDirectory() ) {
    		
    		System.err.println(dirname + " is not a directory.");
    		System.exit(1);
    	}
    	
    	File[] files = dir.listFiles();
    	
    	System.out.println("<work>");
    	for ( File file: files ) {
    		
    		if (file.getName().endsWith(".xhtml")) System.out.printf("<item uri=\"%s\" />\n", file.toURI().toURL());
    	}
    	System.out.println("</work>");
    	
	}


}
