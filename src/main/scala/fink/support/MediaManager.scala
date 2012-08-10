package fink.support

import scala.collection.JavaConversions._

import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.regex.Pattern

import org.apache.commons.fileupload.FileItem
import org.apache.commons.io.IOUtils
import fink.data._

import javax.imageio.ImageIO
import java.security.MessageDigest

// Image specifications
sealed abstract case class ImageSpec(name: String)
case class FullImageSpec(override val name: String) extends ImageSpec(name)
case class KeepRatioImageSpec(override val name: String, max: Int) extends ImageSpec(name)
case class SquareImageSpec(override val name: String, width: Int) extends ImageSpec(name)

sealed abstract case class MediaUpload
case class ImageUpload(hash: String, contentType: String, filename: String)
case class DocumentUpload(hash: String, contentType: String, filename: String)

object MediaManager {

  def baseDirectory = Config.mediaDirectory

  val imageFormats: Map[String, String] = Map(
    "jpg" -> "image/jpg",
    "jpeg" -> "image/jpeg",
    "png" -> "image/png",
    "gif" -> "image/gif"
  )

  val imageExtensions: Map[String, String] = Map(
    "image/jpg" -> "jpg",
    "image/jpeg" -> "jpeg",
    "image/png" -> "png",
    "image/gif" -> "gif"
  )

  val imageSpecs : List[ImageSpec] = List(
    FullImageSpec("full"),
    KeepRatioImageSpec("medium", 300),
    SquareImageSpec("thumb", 100),
    KeepRatioImageSpec("big", 700)
  )

  val FileName = """(.*)\.(.*)""".r

  // check if the base upload folder exists in the filesystem
  protected def sanitizeEnv = {
    checkDirectory(baseDirectory)
  }

  protected def checkDirectory(dir: String) = {
    val target = new File(dir)
    if (!target.exists) target.mkdirs
  }

  protected def inferContentType(item: FileItem, ext: String) = {
    imageFormats.get(ext)
  }

  def processUpload(item: FileItem): Option[ImageUpload] = {
    sanitizeEnv
    for {
      FileName(name, ext) <- Option(item.getName)
      contentType <- inferContentType(item, ext)
      filehash <- hashFilename(baseDirectory, name, ext)
    } yield {
      specs.foreach(spec => processImage(spec, item.getInputStream, hash, ext))
      ImageUpload(contentType, filehash, item.getName)
    }
  }

  protected def processImage(spec: ImageSpec, upload: InputStream, hash: String, ext: String): File = {
    spec match {
      case FullImageSpec(sid) => {
        val target = new File("%s/%s-%s.%s".format(baseDirectory, hash, sid, ext))
        IOUtils.copy(upload, new FileOutputStream(target))
        return target
      }
      case KeepRatioImageSpec(sid, max) => {
        val target = new File("%s/%s-%s.%s".format(baseDirectory, hash, sid, ext))
        scaleImage(upload, new FileOutputStream(target), max)
        return target
      }
      case SquareImageSpec(sid, width) => {
        val target = new File("%s/%s-%s.%s".format(baseDirectory, hash, sid, ext))
        createSquareImage(upload, new FileOutputStream(target), width)
        return target
      }
    }
  }

  /**
   * Returns a free filename. TODO thread-safety
   */
  protected def hashFilename(base: String, name: String, ext: String): Option[String] = {
    var hash = md5(java.util.UUID.randomUUID().toString)
    var temp = new File(base + "/" + hash + "." + ext)

    var count = 1
    while (temp.exists) {
      hash = md5(java.util.UUID.randomUUID().toString)
      temp = new File(base + "/" + hash + "." + ext)
      count += 1

      // safety first =)
      if (count == 1000) return None
    }

    return Some(hash)
  }

  protected def scaleImage(in: InputStream, out: OutputStream, largestDimension: Int): Boolean = {
    try {
      val inImage = ImageIO.read(in)
      var width = inImage.getWidth().toFloat
      var height = inImage.getHeight().toFloat

      if (inImage.getWidth > largestDimension && inImage.getWidth >= inImage.getHeight) {
        val ratio = largestDimension.toFloat / inImage.getWidth().toFloat
        width *= ratio
        height *= ratio
      } else if (inImage.getHeight > largestDimension && inImage.getHeight >= inImage.getWidth) {
        val ratio = largestDimension.toFloat / inImage.getHeight().toFloat
        width *= ratio
        height *= ratio
      }

      val outImage = new BufferedImage(width.toInt, height.toInt, BufferedImage.TYPE_INT_RGB);
      outImage.createGraphics().drawImage(
        inImage.getScaledInstance(width.toInt, height.toInt, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
      ImageIO.write(outImage, "jpg", out);
      return true;
    } catch {
      case e: IOException => {
        e.printStackTrace();
        return false;
      }
    }
  }

  protected def createSquareImage(inFile: InputStream, outFile: OutputStream, width: Int): Boolean = {
    try {
      val img = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
      img.createGraphics().drawImage(
        ImageIO.read(inFile).getScaledInstance(width, width, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
      ImageIO.write(img, "jpg", outFile);
      return true;
    } catch {
      case e: IOException => {
        e.printStackTrace();
        return false;
      }
    }
  }
  
  def md5(s : String) : String = {
    val md5 = MessageDigest.getInstance("MD5")
    md5.reset()
    md5.update(s.getBytes())
    md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
  }
}
