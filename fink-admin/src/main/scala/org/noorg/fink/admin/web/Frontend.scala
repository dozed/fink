package org.noorg.fink.admin.web

import org.joda.time.format.DateTimeFormat
import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport
import scala.collection.JavaConversions._
import org.noorg.fink.data.repository.PostRepository
import org.noorg.fink.admin.support.ApplicationContextProvider
import org.noorg.fink.admin.support.MediaManager
import org.noorg.fink.data.repository.TagRepository
import org.noorg.fink.data.repository.ImageRepository
import org.noorg.fink.data.repository.PageRepository
import org.noorg.fink.data.repository.MediaRepository
import org.noorg.fink.data.entities.Page

class Frontend extends ScalatraServlet with ScalateSupport {

	def layout(template: String, attributes: Map[String, Any]) = templateEngine.layout("/WEB-INF/" + template + ".scaml", attributes)

	def layout(template: String) = templateEngine.layout("/WEB-INF/" + template + ".scaml")

	var postRepository : PostRepository = null
	var mediaRepository : MediaRepository = null
	var imageRepository : ImageRepository = null
	var tagRepository : TagRepository = null
	var pageRepository : PageRepository = null

	var inited = false
	
	def ensureRepositories() = {
		if (!inited) {
		  MediaManager.base = servletContext.getRealPath("/uploads")
		  	
		  println(MediaManager.base)
		  
			postRepository = ApplicationContextProvider.getContext().getBean(classOf[PostRepository])
			mediaRepository = ApplicationContextProvider.getContext().getBean(classOf[MediaRepository])
			imageRepository = ApplicationContextProvider.getContext().getBean(classOf[ImageRepository])
			tagRepository = ApplicationContextProvider.getContext().getBean(classOf[TagRepository])
			pageRepository = ApplicationContextProvider.getContext().getBean(classOf[PageRepository])

			inited = true
		}
	}
	
	before {
		contentType = "text/html"
		ensureRepositories()
	}
	get("/") {
		templateEngine.layout("/WEB-INF/index.scaml", Map("content" -> "Hello World"))
	}

	get("/post/:year/:month/:day/:title") {
		val year = params("year").toInt
		val month = params("month").toInt
		val day = params("day").toInt
		val title = params("title")
		val post = postRepository.findPost(year, month, day, title)
		
		val fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		
		layout("post", Map("post" -> post, "date" -> fmt.print(post.getDate())))
	}
	
	get("/about") {
		templateEngine.layout("/WEB-INF/about.scaml", Map("content" -> "Hello World"))
	}

	get("/posts") {
		layout("read", Map("content" -> postRepository.getEntries()))
	}

	get("/dates") {
		templateEngine.layout("/WEB-INF/dates.scaml", Map("content" -> postRepository.getEntries()))
	}

	protected def contextPath = request.getContextPath

}