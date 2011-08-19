package org.noorg.fink.admin.web

import com.codahale.jerkson.Json._
import org.apache.commons.fileupload.FileItem
import org.noorg.fink.admin.support.ApplicationContextProvider
import org.noorg.fink.admin.support.MediaManager
import org.noorg.fink.data.entities.Page
import org.noorg.fink.data.entities.Tag
import org.noorg.fink.data.repository.ImageRepository
import org.noorg.fink.data.repository.MediaRepository
import org.noorg.fink.data.repository.PageRepository
import org.noorg.fink.data.repository.PostRepository
import org.noorg.fink.data.repository.TagRepository
import org.scalatra.ScalatraServlet
import org.scalatra.fileupload.FileUploadSupport
import org.scalatra.scalate.ScalateSupport
import scala.collection.JavaConversions._

//cant use controller here, since the servlet is added via web.xml
//@Controller
class Admin extends ScalatraServlet with ScalateSupport with FileUploadSupport {

	def layout(template: String, attributes: Map[String, Any]) = templateEngine.layout("/WEB-INF/" + template + ".scaml", attributes + ("layout" -> "/WEB-INF/scalate/layouts/admin.scaml"))

	def layout(template: String) = templateEngine.layout("/WEB-INF/" + template + ".scaml", Map("layout" -> "/WEB-INF/scalate/layouts/admin.scaml"))

	def render(template: String, attributes: Map[String, Any]) = createRenderContext.render("/WEB-INF/" + template + ".scaml", attributes)

	def render(template: String) = renderTemplate("/WEB-INF/" + template + ".scaml")

	def uri(uri: String) = {
		if (uri.startsWith("/")) {
			request.getContextPath + uri
		} else {
			uri
		}
	}

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
			
			var page = pageRepository.find("title", "Website")
			if (page == null) {
			  page = new Page("Website")
			  pageRepository.save(page)
			}
			
			inited = true
		}
	}
	
	before {
		contentType = "text/html"
		ensureRepositories()
	}

	get("/") {
		layout("admin.index", Map("content" -> "Hello World"))
	}

	get("/config") {
		//SetupTool.initDatabase();
	}

	get("/pages") {
		//pageRepository.findAll().foreach { p => println(p) }
//		val test = pageRepository.find("title", "Test")
//		println(test.getParentPage().getParentPage())
//		test.getParentPage().getSubPages().foreach { p => println(p) }
		//pageRepository.find("title", "People").getSubPages().foreach { p => println(p) }
		val root = pageRepository.find("title", "Website")
		goDown(root)
		println(generate(root))

		layout("pages.index", Map("pages" -> pageRepository.findAll, "pagesJson" -> generate(root)))
	}

	def goDown(page: Page) {
		println(page.getId + ": " + page.getTitle)
		for (p <- page.getSubPages()) {
			goDown(p)
		}
	}

	get("/pages/create") {
		layout("pages.create", Map("rootPage" -> pageRepository.find("title", "Website")))
	}

	post("/pages/create") {
		val parent = pageRepository.findPageByUuid(params("parent"))
		val page = new Page(params("title"), params("author"))
		parent.addPage(page)
		pageRepository.save(page)
		redirect(uri("/admin/pages"))
		"ok"
	}

	get("/pages/edit/:uuid") {
		val page = pageRepository.findPageByUuid(params("uuid"))
		val rootPage = pageRepository.find("title", "Website")
		layout("pages.edit", Map("page" -> page, "rootPage" -> rootPage))
	}

	post("/pages/edit/:uuid") {
		val page = pageRepository.findPageByUuid(params("uuid"))
		val parent = pageRepository.findPageByUuid(params("parent"))
		val oldParent = page.getParentPage

		if (oldParent != null && oldParent != parent) {
			parent.addPage(page)
			pageRepository.save(oldParent)
		}

		page.clearTags
		
		for (t <- params("tags").split(",")) {
			var tag = tagRepository.findTag(t)
			if (tag == null) {
				tag = tagRepository.createTag(t)
			}
			page.addTag(tag)
		}

		page.setTitle(params("title"))
		page.setAuthor(params("author"))
		page.setText(params("text"))
		
		pageRepository.save(page)
		pageRepository.save(parent)
  
		redirect(uri("/admin/pages"))
	}

	get("/posts") {
		layout("posts.index", Map("posts" -> postRepository.getEntries))
	}

	get("/posts/create") {
		layout("posts.create")
	}

	post("/posts/create") {
		postRepository.createPost(params("title"), params("text"), params("author"), params("category"), params("tags"))
		redirect(uri("/admin/posts"))
	}

	get("/posts/edit/:uuid") {
		val post = postRepository.findPostByUuid(params("uuid"))
		layout("posts.edit", Map("post" -> post))
	}

	post("/posts/edit/:uuid") {
		val post = postRepository.findPostByUuid(params("uuid"))

		post.clearTags
		
		for (t <- params("tags").split(",")) {
			var tag = tagRepository.findTag(t)
			if (tag == null) {
				tag = tagRepository.createTag(t)
			}
			post.addTag(tag)
		}

		post.setTitle(params("title"))
		post.setAuthor(params("title"))
		post.setText(params("text"))
		
		postRepository.save(post)
		
		redirect(uri("/admin/posts"))
	}

	get("/collections") {
		layout("collections.index", Map("collections" -> mediaRepository.getCollections))
	}

	get("/collections/create") {
		render("collections.create")
	}

	get("/collections/list") {
		render("collections.list")
	}

	post("/collections/create") {
		val c = mediaRepository.createCollection(params("title"))
		//redirect(uri("/admin/collections/edit/" + c.getUuid))
		c.getUuid
	}

	get("/collections/edit/:id") {
		val c = mediaRepository.findCollection(params("id"))
		render("collections.edit", Map("collection" -> c))
	}

	get("/collections/edit/:id/images") {
		val c = mediaRepository.findCollection(params("id"))
		render("collections.images", Map("collection" -> c))
	}

	post("/collections/edit/:id/update") {
		val title = params("title")
		val author = params("author")
		val tags = params("tags")

		val c = mediaRepository.findCollection(params("id"))

		for (t <- tags.split(",")) {
			var tag = tagRepository.findTag(t)
			if (tag == null) {
				tag = tagRepository.createTag(t)
			}
			c.addTag(tag)
		}

		c.setTitle(title)
		c.setAuthor(author)
		mediaRepository.save(c)
		println("updated")
	}

	post("/collections/edit/:id/setcover") {
		val collection = mediaRepository.findCollection(params("id"))
		val image = imageRepository.findImage(params("mediaid"))
		collection.setCover(image)
		mediaRepository.save(collection)
		println("updated")
	}

	post("/collections/edit/:id/add") {
		println(fileParams("file"))
		val collection = mediaRepository.findCollection(params("id"))
		val image = MediaManager.processUpload(fileParams("file"))
		collection.addItem(image)
		mediaRepository.save(collection)
		println("updated")
		//redirect(uri("/admin/collections/edit/" + collection.getUuid))
	}

	post("/collections/edit/:id/delete") {
		"ok"
	}

	get("/images") {
		layout("admin.images", Map("images" -> MediaManager.getImagesList))
	}

	get("/images/upload") {
		"ok"
	}

	post("/images/upload") {
		val item = fileParams("file")
		MediaManager.processUpload(item)
		"oK2"
	}

	get("/images/find") {
		render("images", Map("images" -> MediaManager.getImagesList))
	}

	get("/guess/*") {
		"You missed!"
	}

	get("/guess/:who") {
		params("who") match {
			case "Stefan" => "You got me!"
			case _ => pass()
		}
	}

	//  	notFound {
	//  		<h1>Not found.  Bummer.</h1>
	//  	}

	protected def contextPath = request.getContextPath

}
