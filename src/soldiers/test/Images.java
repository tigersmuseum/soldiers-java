package soldiers.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Images {

	public static void main(String[] args) throws IOException {

		BufferedImage img;
		
		img = ImageIO.read(new File("H:\\Archive\\Admin\\Database\\eclipse-workspace\\Tigers\\images\\(99) Officers SG, HPB, Variant.JPG"));
		
		System.out.println(img.getWidth() + ","+ img.getHeight());
	}

}
