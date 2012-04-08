package fink.web

import scala.collection.JavaConversions._
import java.lang.{Long=>JLong}

import fink.data.MediaCollection
import fink.data.Page
import fink.data.Post
import fink.data.Image
import fink.data.Tag
import fink.data.Category
import fink.data.ImageRepository
import fink.data.MediaRepository
import fink.data.PageRepository
import fink.data.PostRepository
import fink.data.TagRepository
import fink.data.RepositorySupport
import fink.support.MediaManager

import org.scalatra.scalate.ScalateSupport
import org.scalatra.ScalatraServlet
import org.scalatra.servlet._

import org.joda.time.format.DateTimeFormat

class Frontend extends ScalatraServlet with ScalateSupport with RepositorySupport {

	def templateBase = "/WEB-INF/frontend"
	
	def layout(template: String) = {
		templateAttributes("mediaRepository") = mediaRepository
		templateAttributes("layout") = (templateBase + "/layouts/admin.jade")
		jade(templateBase + "/" + template + ".jade")
	}
	
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
		// layout("admin.index")
		templateAttributes("layout") = (templateBase + "/layouts/default.jade")
		jade(templateBase + "/index.jade")	
	}

	get("/post/:year/:month/:day/:title") {
		val year = params("year").toInt
		val month = params("month").toInt
		val day = params("day").toInt
		val title = params("title")
		
		postRepository.findPost(year, month, day, title) match {
			case Some(post) =>
				val fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
				templateAttributes("post") = post
				templateAttributes("date") = fmt.print(post.date)
				layout("post")
			case None =>
		}
	}

	notFound {
		<h1>Not found.  Bummer.</h1>
	}

}
