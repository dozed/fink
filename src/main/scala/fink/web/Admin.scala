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

	override def init(config: ServletConfig) {
		super.init(config)
		MediaManager.base = config.getServletContext().getRealPath("/uploads")
	}

	before() {
		contentType = "text/html"
	}

	get("/") {
		if (request.getPathInfo == null) redirect("/admin/")
		templateAttributes("layout") = ("/admin/layouts/default.jade")
		jade("/admin/index.jade")	
	}

	notFound {
		halt(404, <h1>Not found.  Bummer.</h1>)
	}

}
