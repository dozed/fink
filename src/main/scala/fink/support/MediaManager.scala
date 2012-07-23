// package fink.support

// import scala.collection.JavaConversions._

// import java.awt.image.BufferedImage
// import java.io.File
// import java.io.FileOutputStream
// import java.io.IOException
// import java.io.InputStream
// import java.io.OutputStream
// import java.util.regex.Pattern

// import org.apache.commons.fileupload.FileItem
// import org.apache.commons.io.IOUtils
// import fink.data.Image
// import fink.data.ImageRepository

// import javax.imageio.ImageIO
// import java.security.MessageDigest

// // Image specifications
// sealed abstract case class ImageSpec(name: String)
// case class FullImageSpec(override val name: String) extends ImageSpec(name)
// case class KeepRatioImageSpec(override val name: String, max: Int) extends ImageSpec(name)
// case class SquareImageSpec(override val name: String, width: Int) extends ImageSpec(name)

// object MediaManager {

// 	var base = "./uploads"
// 	def getBase() = base
// 	def relativeDirectory(sub: String) = getBase() + "/" + sub
// 	def tmpDirectory = getBase() + "/tmp"

// 	// todo refactor this -> fink.properties
// 	def dirImagesFull = getBase + "/full"
// 	def dirImagesThumb = getBase + "/thumbs"
// 	def dirImagesMedium = getBase + "/medium"

// 	val specs = Array(
// 		FullImageSpec("full"),
// 		KeepRatioImageSpec("medium", 300),
// 		SquareImageSpec("thumb", 100),
// 		KeepRatioImageSpec("big", 700)
// 	)

// 	var imageRepository = new ImageRepository

// 	// setup the folder structure in the filesystem
// 	// also set repositories, later remove this
// 	protected def sanitizeEnv = {
// 		checkDirectory(base)
// 		specs.foreach(spec => checkDirectory(relativeDirectory(spec.name)))
// 	}

// 	protected def checkDirectory(dir: String) = {
// 		val target = new File(dir)
// 		if (!target.exists) target.mkdirs
// 	}

// 	// TODO use a more sophisticated method to check for an image
// 	protected def isImage(item: FileItem, name: String, ext: String): Boolean = true

// 	def getImagesList: List[Image] = {
// 		sanitizeEnv
// 		imageRepository.findAll().toList
// 	}

// 	def getImages: List[String] = {
// 		new File(dirImagesFull).listFiles().filter({ f =>
// 			""".*\.(jpg|jpeg|gif|png)$""".r.findFirstIn(f.getName).isDefined
// 		}).map(f => f.getName).toList
// 	}

// 	def getThumbnails: List[String] = {
// 		sanitizeEnv
// 		new File(dirImagesThumb).listFiles().filter({ f =>
// 			""".*\.(jpg|jpeg|gif|png)$""".r.findFirstIn(f.getName).isDefined
// 		}).map(f => f.getName).toList
// 	}

// 	def processUpload(item: FileItem): Option[Image] = {
// 		sanitizeEnv
// 		val m = Pattern.compile("(.*)\\.(.*)$").matcher(item.getName)
// 		if (!m.matches) return null

// 		val (name, ext) = (m.group(1), m.group(2))
// 		var img : Image = null
		
// 		try {
// 			if (isImage(item, name, ext)) {
// 				img = processImage(item, name, ext)
// 			}
// 			return Some(img)
// 		} catch {
// 			case e: Exception => {
// 				e.printStackTrace()
// 				return None
// 			}
// 		}
// 	}

// 	protected def processImage(item: FileItem, name: String, ext: String): Image = {
// 		var m = Map[String, File]()
// 		specs.foreach(spec => m += (spec.name -> processImage(spec, item.getInputStream, name, ext)))
// 		return imageRepository.createImage(name, m("full").getName(), m("medium").getName(), m("thumb").getName())
// 	}

// 	protected def processImage(spec: ImageSpec, upload: InputStream, fileBaseName: String, ext: String): File = {
// 		spec match {
// 			case FullImageSpec(n) => {
// 				val target = getFileResource(relativeDirectory(n), md5(fileBaseName), ext)
// 				IOUtils.copy(upload, new FileOutputStream(target))
// 				return target
// 			}
// 			case KeepRatioImageSpec(n, max) => {
// 				val target = getFileResource(relativeDirectory(n), md5(fileBaseName), ext)
// 				scaleImage(upload, new FileOutputStream(target), max)
// 				return target
// 			}
// 			case SquareImageSpec(n, width) => {
// 				val target = getFileResource(relativeDirectory(n), md5(fileBaseName), ext)
// 				createSquareImage(upload, new FileOutputStream(target), width)
// 				return target
// 			}
// 		}
// 	}

// 	/**
// 	 * Returns a file instance for the choosen absolute filename. Modifies that name by adding digits if there exists already
// 	 * a file with that name.
// 	 */
// 	protected def getFileResource(base: String, name: String, ext: String): File = {
// 		val fill = ""
// 		var temp = new File(base + "/" + name + "." + ext)
// 		var count = 1
// 		while (temp.exists) {
// 			temp = new File(base + "/" + name + "-" + count.formatted("%03d") + "." + ext)
// 			count += 1

// 			// safety first =)
// 			if (count == 1000) return null
// 		}
// 		return temp
// 	}

// 	protected def scaleImage(in: InputStream, out: OutputStream, largestDimension: Int): Boolean = {
// 		try {
// 			val inImage = ImageIO.read(in)
// 			var width = inImage.getWidth().toFloat
// 			var height = inImage.getHeight().toFloat

// 			if (inImage.getWidth > largestDimension && inImage.getWidth >= inImage.getHeight) {
// 				val ratio = largestDimension.toFloat / inImage.getWidth().toFloat
// 				width *= ratio
// 				height *= ratio
// 			} else if (inImage.getHeight > largestDimension && inImage.getHeight >= inImage.getWidth) {
// 				val ratio = largestDimension.toFloat / inImage.getHeight().toFloat
// 				width *= ratio
// 				height *= ratio
// 			}

// 			val outImage = new BufferedImage(width.toInt, height.toInt, BufferedImage.TYPE_INT_RGB);
// 			outImage.createGraphics().drawImage(
// 				inImage.getScaledInstance(width.toInt, height.toInt, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
// 			ImageIO.write(outImage, "jpg", out);
// 			return true;
// 		} catch {
// 			case e: IOException => {
// 				e.printStackTrace();
// 				return false;
// 			}
// 		}
// 	}

// 	protected def createSquareImage(inFile: InputStream, outFile: OutputStream, width: Int): Boolean = {
// 		try {
// 			val img = new BufferedImage(width, width, BufferedImage.TYPE_INT_RGB);
// 			img.createGraphics().drawImage(
// 				ImageIO.read(inFile).getScaledInstance(width, width, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
// 			ImageIO.write(img, "jpg", outFile);
// 			return true;
// 		} catch {
// 			case e: IOException => {
// 				e.printStackTrace();
// 				return false;
// 			}
// 		}
// 	}
	
// 	def md5(s : String) : String = {
// 		val md5 = MessageDigest.getInstance("MD5")
// 		md5.reset()
// 		md5.update(s.getBytes())
// 		md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
// 	}
// }
