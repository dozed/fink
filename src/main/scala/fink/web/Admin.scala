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

class Admin extends ScalatraServlet with RepositorySupport with ResourcesSupport with AuthenticationRoutes with ScalateSupport with FileUploadSupport {

	override def destroy() {
		ContentItemRepository.shutdown()
	}

	implicit override val jsonFormats = Serialization.formats(ShortTypeHints(List(classOf[Page], classOf[Category]))) + FieldSerializer[Post]()

	def adminTemplateBase = "/WEB-INF/admin"

	def layout(template: String) = {
		templateAttributes("mediaRepository") = mediaRepository
		templateAttributes("layout") = (adminTemplateBase + "/layouts/admin.jade")
		scaml(adminTemplateBase + "/" + template + ".scaml")	
	}

	def render(template: String) = {
		templateAttributes("mediaRepository") = mediaRepository
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
		MediaManager.base = "/tmp/foo"
		// pageRepository.find("title", "Website") match {
		// 	case Some(page) =>
		// 	case None =>
		// 		val page = Page(title = "Website")
		// 		pageRepository.save(page)
		// }
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
		<h1>Not found.  Bummer.</h1>
	}

}
