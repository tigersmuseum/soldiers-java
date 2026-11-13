package soldiers.test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class ScratchDate {

	public static Pattern dates1 = Pattern.compile("(\\d{1,2})(st|nd|rd|th)?\\s+(jan|january|feb|february|mar|march|apr|april|may|jun|june|jul|july|aug|august|sep|september|oct|october|nov|november|dec|december),?\\s+(\\d{2,4})\\b", Pattern.CASE_INSENSITIVE);
	public static Pattern dates2 = Pattern.compile("(jan|january|feb|february|mar|march|apr|april|may|jun|june|jul|july|aug|august|sep|september|oct|october|nov|november|dec|december)\\s+(\\d{1,2})(st|nd|rd|th)?,?\\s+(\\d{2,4})\\b", Pattern.CASE_INSENSITIVE);

	public static void main(String[] args) throws ParseException, IOException {

		List<String> lines = FileUtils.readLines(new File("/C:/Users/Archive.SERLESHOUSE/Documents/GitHub/history/docs/gazette.txt"), "windows-1252");

		Map<String, String> dateMap = new HashMap<>();
		
		collectDates1(lines, dates1, dateMap);
		collectDates2(lines, dates2, dateMap);
		
		System.out.println(dateMap.values());
		System.out.println(dateMap.keySet().size() + " dates");
	}

	public static void collectDates1(List<String> lines, Pattern datePattern, Map<String, String> map) throws ParseException {
		
		for ( String text: lines  ) {
			
			Matcher matcher = datePattern.matcher(text);

			while ( matcher.find() ) {

				String date = String.format("%02d %s %4d", Integer.valueOf(matcher.group(1)), matcher.group(3), Integer.valueOf(matcher.group(4)));				
				map.put(matcher.group(0), parseDate(date));
			}
		}		
	}

	public static void collectDates2(List<String> lines, Pattern datePattern, Map<String, String> map) throws ParseException {
		
		for ( String text: lines  ) {
			
			Matcher matcher = datePattern.matcher(text);

			while ( matcher.find() ) {

				String date = String.format("%02d %s %4d", Integer.valueOf(matcher.group(2)), matcher.group(1), Integer.valueOf(matcher.group(4)));				
				map.put(matcher.group(0), parseDate(date));
			}
		}		
	}
	
	public static String parseDate(String date) throws ParseException {

		SimpleDateFormat in  = new SimpleDateFormat("d MMM y");
		SimpleDateFormat out = new SimpleDateFormat("yyyy-MM-dd");
		return out.format(in.parse(date));
	}
}
