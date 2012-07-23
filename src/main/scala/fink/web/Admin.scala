package fink.web

import scala.collection.JavaConversions._
import java.lang.{Long=>JLong}

import fink.data._
import fink.support._

import org.scalatra.fileupload.FileUploadSupport
import org.scalatra.scalate.ScalateSupport
import org.scalatra.ScalatraServlet
import org.scalatra.servlet._

import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

class Admin extends ScalatraServlet with RepositorySupport with AuthenticationRoutes with ResourcesSupport with ScalateSupport with FileUploadSupport {

	// override def destroy() {
	// 	ContentItemRepository.shutdown()
	// }

	override implicit val jsonFormats = Serialization.formats(ShortTypeHints(List(classOf[Page], classOf[Category], classOf[Tag]))) + FieldSerializer[Post]()  + FieldSerializer[Gallery]() + FieldSerializer[Image]()

	def adminTemplateBase = "/WEB-INF/admin"

	def layout(template: String) = {
		// templateAttributes("mediaRepository") = mediaRepository
		templateAttributes("layout") = (adminTemplateBase + "/layouts/admin.jade")
		scaml(adminTemplateBase + "/" + template + ".scaml")	
	}

	def render(template: String) = {
		// templateAttributes("mediaRepository") = mediaRepository
		layoutTemplate(adminTemplateBase + "/" + template + ".scaml", "layout" -> "") 
	}

	def uri(uri: String) = {
		if (uri.startsWith("/")) {
			request.getContextPath + uri
		} else {
			uri
		}
	}
 
	// wtf
	sanitize()

	def sanitize() = {
		// MediaManager.base = servletContext.getRealPath("/uploads")
		// MediaManager.base = "/tmp/foo"
	}

	before() {
		contentType = "text/html"
	}

	get("/") {
		if (request.getPathInfo == null) redirect("/admin/")
		templateAttributes("layout") = (adminTemplateBase + "/layouts/coffee.jade")
		jade(adminTemplateBase + "/index.jade")	
	}

	notFound {
		halt(404, <h1>Not found.  Bummer.</h1>)
	}

}
