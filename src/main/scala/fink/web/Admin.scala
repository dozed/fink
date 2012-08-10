package fink.web

import scala.collection.JavaConversions._
import java.lang.{Long=>JLong}

import fink.data._
import fink.support._

import javax.servlet.ServletConfig

import org.scalatra.fileupload.FileUploadSupport
import org.scalatra.scalate.ScalateSupport
import org.scalatra.ScalatraServlet
import org.scalatra.servlet._

import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import java.io.File

class Admin extends ScalatraServlet with RepositorySupport with AuthenticationRoutes with ResourceRoutes with ScalateSupport with FileUploadSupport {

  override implicit val jsonFormats = Serialization.formats(ShortTypeHints(List(classOf[Page], classOf[Category], classOf[Tag]))) + FieldSerializer[Post]()  + FieldSerializer[Gallery]() + FieldSerializer[Image]()

  def adminTemplateBase = "/WEB-INF/admin"

  def uri(uri: String) = {
    if (uri.startsWith("/")) {
      request.getContextPath + uri
    } else {
      uri
    }
  }

  before() {
    contentType = "text/html"
  }

  get("/") {
    if (request.getPathInfo == null) redirect("/admin/")
    templateAttributes("layout") = ("/admin/layouts/default.jade")
    jade("/admin/index.jade") 
  }

  get("/uploads/images/:hash/:spec/:file") {
    (for {
      hash <- Option(params("hash"))
      image <- imageRepository.byHash(hash)
      ext <- MediaManager.imageExtensions.get(image.contentType)
      spec <- MediaManager.imageSpecs.filter(_.name == params("spec")).headOption
    } yield {
      val file = new File("%s/%s-%s.%s".format(Config.mediaDirectory, image.hash, spec.name, ext))
      if (!file.exists) halt(404)
      response.addHeader("Content-Disposition", "inline;filename=\"%s\"".format(image.filename))
      response.addHeader("Content-type", image.contentType)
      file
    }) getOrElse(halt(404))
  }

  notFound {
    halt(404, <h1>Not found.  Bummer.</h1>)
  }

}
