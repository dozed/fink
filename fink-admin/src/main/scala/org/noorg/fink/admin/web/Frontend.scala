package org.noorg.fink.admin.web

import org.joda.time.format.DateTimeFormat
import org.noorg.fink.admin.support.ApplicationContextProvider
import org.noorg.fink.admin.support.MediaManager
import org.noorg.fink.data.entities.Page
import org.noorg.fink.data.repositories.ImageRepository
import org.noorg.fink.data.repositories.MediaRepository
import org.noorg.fink.data.repositories.PageRepository
import org.noorg.fink.data.repositories.PostRepository
import org.noorg.fink.data.repositories.TagRepository
import org.scalatra.scalate.ScalateSupport
import org.scalatra.ScalatraServlet

class Frontend extends ScalatraServlet with ScalateSupport {

	val themeName = "simple"

	def themeBase = "/WEB-INF/themes/" + themeName
		
	def layout(template: String, attributes : Map[String, Any] = Map[String, Any]()) = {
		templateAttributes("mediaRepository") = mediaRepository
		templateAttributes("pageRepository") = pageRepository
		templateAttributes("rootPage") = pageRepository.find("title", "Website")
		templateAttributes("layout") = (themeBase + "/layouts/default.scaml")
		scaml(themeBase + "/" + template + ".scaml")
	}

	var postRepository: PostRepository = null
	var mediaRepository: MediaRepository = null
	var imageRepository: ImageRepository = null
	var tagRepository: TagRepository = null
	var pageRepository: PageRepository = null

	var inited = false

	def ensureRepositories() = {
		if (!inited) {
			MediaManager.base = servletContext.getRealPath("/uploads")

			postRepository = ApplicationContextProvider.getContext().getBean(classOf[PostRepository])
			mediaRepository = ApplicationContextProvider.getContext().getBean(classOf[MediaRepository])
			imageRepository = ApplicationContextProvider.getContext().getBean(classOf[ImageRepository])
			tagRepository = ApplicationContextProvider.getContext().getBean(classOf[TagRepository])
			pageRepository = ApplicationContextProvider.getContext().getBean(classOf[PageRepository])

			var page = pageRepository.find("title", "Website")
			if (page == null) {
				page = new Page("Website")
				pageRepository.save(page)
			}

			inited = true
		}
	}

	before() {
		contentType = "text/html"
		ensureRepositories
	}

	get("/") {
		layout("index", Map("content" -> "Hello World"))
	}

	get("/post/:year/:month/:day/:title") {
		val year = params("year").toInt
		val month = params("month").toInt
		val day = params("day").toInt
		val title = params("title")
		val post = postRepository.findPost(year, month, day, title)

		val fmt = DateTimeFormat.forPattern("yyyy-MM-dd");

		templateAttributes("post") = post
		templateAttributes("date") = fmt.print(post.getDate()) // util..
		layout("post")
	}

	get("/about") {
		layout("about")
	}

	get("/posts") {
	  templateAttributes("posts") = postRepository.getEntries()
		layout("read")
	}

	get("/dates") {
	  templateAttributes("posts") = postRepository.getEntries()
		layout("dates")
	}
	
	get("/pages/:shortlink") {
		val shortlink = params("shortlink")
		val page = pageRepository.findPageByShortlink(shortlink)
		templateAttributes("page") = page
		layout("page")
	}

	get("/collections/:shortlink") {
		val shortlink = params("shortlink")
		val collection = mediaRepository.findCollectionByShortlink(shortlink)
		templateAttributes("collection") = collection
		layout("collection")
	}
	
	notFound {
 		<h1>Not found.  Bummer.</h1>
	}

	protected def contextPath = request.getContextPath

}