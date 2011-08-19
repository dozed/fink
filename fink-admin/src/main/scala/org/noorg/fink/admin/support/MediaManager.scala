package org.noorg.fink.admin.support

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.regex.Pattern
import org.apache.commons.fileupload.FileItem
import org.noorg.fink.data.entities.Image
import org.noorg.fink.data.repository.ImageRepository
import org.noorg.fink.data.repository.MediaRepository
import scala.collection.JavaConversions._

object MediaManager {

  val base = "src/main/webapp/uploads"
  val dirImagesFull = base + "/images"
  val dirImagesThumb = base + "/thumbs"
  val dirImagesMedium = base + "/medium"

  var inited = false 
  var imageRepository : ImageRepository = null
	var mediaRepository : MediaRepository = null
  
  // setup the folder structure in the filesystem
	// also set repositories, later remove this
  protected def sanitizeEnv = {
    checkDirectory(base)
    checkDirectory(dirImagesFull)
    checkDirectory(dirImagesMedium)
    checkDirectory(dirImagesThumb)
    
    if (!inited) {
    	imageRepository = ApplicationContextProvider.getContext().getBean(classOf[ImageRepository])
    	mediaRepository = ApplicationContextProvider.getContext().getBean(classOf[MediaRepository])
      inited = true
    }
  }

  protected def checkDirectory(dir: String) = {
    val target = new File(dir)
    if (!target.exists) target.mkdirs
  }

  // TODO use a more sophisticated method to check for an image
  protected def isImage(item: FileItem, name: String, ext: String): Boolean = true

  def getImagesList: List[Image] = {
    sanitizeEnv
    imageRepository.findAll().toList
  }

  def getImages: List[String] = {
    sanitizeEnv
    new File(dirImagesFull).listFiles().filter({ f =>
      """.*\.(jpg|jpeg|gif|png)$""".r.findFirstIn(f.getName).isDefined
    }).map(f => f.getName).toList
  }

  def getThumbnails: List[String] = {
    sanitizeEnv
    new File(dirImagesThumb).listFiles().filter({ f =>
      """.*\.(jpg|jpeg|gif|png)$""".r.findFirstIn(f.getName).isDefined
    }).map(f => f.getName).toList
  }

  def processUpload(item: FileItem): Image = {
    sanitizeEnv
    val m = Pattern.compile("(.*)\\.(.*)$").matcher(item.getName)
    if (!m.matches) return null

    val (name, ext) = (m.group(1), m.group(2))

    if (isImage(item, name, ext)) {
      return processImage(item, name, ext)
    }

    return null
  }

  protected def processImage(item: FileItem, name: String, ext: String): Image = {
    val full = getFileResource(dirImagesFull, name, ext)
    item.write(full)

    //?
    //val t = new Thumbnail()

    val thumb = new File(dirImagesThumb + "/" + full.getName)
    var status = resizeImage(full, thumb, 100)

    val medium = new File(dirImagesMedium + "/" + full.getName)
    scaleImage(full, medium, 500)

    return imageRepository.addImage(full.getName, full.getName, medium.getName, thumb.getName)
  }

  /**
   * Returns a file instance for the choosen absolute filename. Modifies that name by adding digits if there exists already
   * a file with that name.
   */
  protected def getFileResource(base: String, name: String, ext: String): File = {
    val fill = ""
    var temp = new File(base + "/" + name + "." + ext)
    var count = 1
    while (temp.exists) {
      temp = new File(base + "/" + name + "-" + count.formatted("%03d") + "." + ext)
      count += 1

      // safety first =)
      if (count == 1000) return null
    }
    return temp
  }

  protected def scaleImage(inFile: File, outFile: File, largestDimension: Int): Boolean = {
    try {
      val inImage = ImageIO.read(inFile)
      var width = inImage.getWidth().toFloat
      var height = inImage.getHeight().toFloat

      if (inImage.getWidth > largestDimension && inImage.getWidth > inImage.getHeight) {
        val ratio = largestDimension.toFloat / inImage.getWidth().toFloat
        width *= ratio
        height *= ratio
      } else if (inImage.getHeight > largestDimension && inImage.getHeight > inImage.getWidth) {
        val ratio = largestDimension.toFloat / inImage.getHeight().toFloat
        width *= ratio
        height *= ratio
      }

      val img = new BufferedImage(width.toInt, height.toInt, BufferedImage.TYPE_INT_RGB);
      img.createGraphics().drawImage(
        ImageIO.read(inFile).getScaledInstance(width.toInt, height.toInt, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
      ImageIO.write(img, "jpg", outFile);
      return true;
    } catch {
      case e: IOException => {
        e.printStackTrace();
        return false;
      }
    }
  }

  protected def resizeImage(inFile: File, outFile: File, largestDimension: Int): Boolean = {
    try {
      val img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
      img.createGraphics().drawImage(
        ImageIO.read(inFile).getScaledInstance(100, 100, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
      ImageIO.write(img, "jpg", outFile);
      return true;
    } catch {
      case e: IOException => {
        e.printStackTrace();
        return false;
      }
    }
  }
}