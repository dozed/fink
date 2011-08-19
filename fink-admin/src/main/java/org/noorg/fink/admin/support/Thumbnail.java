package org.noorg.fink.admin.support;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

class Thumbnail {

	public Thumbnail() {
		
	}
	
	public boolean createThumbnail(String inFilename, String outFilename, int largestDimension) {
		try {
			BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
			img.createGraphics().drawImage(
					ImageIO.read(new File(inFilename)).getScaledInstance(100, 100, Image.SCALE_SMOOTH), 0, 0, null);
			ImageIO.write(img, "jpg", new File(outFilename));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
