package soldiers.test;

import java.util.List;

import soldiers.database.Normalize;
import soldiers.database.Person;
import soldiers.text.Parser;

public class ParserTest {
	
	public static void main(String[] args) {

		String surface = "3117 Pte T Jarvis";
		
		String text = surface.replaceAll("\\p{javaSpaceChar}", " ").trim();
				
		List<Person> list = Parser.findMention(text);
		Normalize.normalizeRank(list);
		
		for ( Person p: list ) {
			
			System.out.printf("(%d) %s = %s", p.getSoldierId(), p.getContent(), p.getSurfaceText());
		}
	}
	
	
}
