package fink.support

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException

import javax.imageio.ImageIO

class Thumbnail {

	def createThumbnail(inFilename: String, outFilename: String, largestDimension: Int): Boolean = {
		try {
			val img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
			img.createGraphics().drawImage(
				ImageIO.read(new File(inFilename)).getScaledInstance(100, 100, Image.SCALE_SMOOTH), 0, 0, null)
			ImageIO.write(img, "jpg", new File(outFilename));
			return true;
		} catch {
			case e: IOException =>
				e.printStackTrace()
				return false
		}
	}

}
