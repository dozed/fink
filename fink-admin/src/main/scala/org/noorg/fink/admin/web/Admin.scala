package org.noorg.fink.admin.web

import scala.collection.JavaConversions.asScalaSet

import org.noorg.fink.admin.support.ApplicationContextProvider
import org.noorg.fink.admin.support.MediaManager
import org.noorg.fink.data.entities.MediaCollection
import org.noorg.fink.data.entities.Page
import org.noorg.fink.data.repositories.ImageRepository
import org.noorg.fink.data.repositories.MediaRepository
import org.noorg.fink.data.repositories.PageRepository
import org.noorg.fink.data.repositories.PostRepository
import org.noorg.fink.data.repositories.TagRepository
import org.scalatra.fileupload.FileUploadSupport
import org.scalatra.scalate.ScalateSupport
import org.scalatra.ScalatraServlet

import com.codahale.jerkson.Json.generate

class Admin extends ScalatraServlet with ScalateSupport with FileUploadSupport {

	def adminTemplateBase = "/WEB-INF/admin"

	def layout(template: String) = {
		templateAttributes("mediaRepository") = mediaRepository
		templateAttributes("layout") = (adminTemplateBase + "/layouts/admin.scaml")
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

	var postRepository: PostRepository = null
	var mediaRepository: MediaRepository = null
	var imageRepository: ImageRepository = null
	var tagRepository: TagRepository = null
	var pageRepository: PageRepository = null

	var inited = false

	def ensureRepositories = {
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
		layout("admin.index")
	}

	get("/configuration") {
		layout("admin.config")
	}

	get("/pages") {
		val root = pageRepository.find("title", "Website")
		//goDown(root)
		//println(generate(root))
		templateAttributes("rootPage") = root

		layout("pages.index")
	}

	def goDown(page: Page) {
		println(page)
		println(page.getId + ": " + page.getTitle)
		for (p <- page.getSubPages()) {
			goDown(p)
		}
	}

	get("/pages/create") {
		templateAttributes("rootPage") = pageRepository.find("title", "Website")
		layout("pages.create")
	}

	post("/pages/create") {
		val parent = pageRepository.findPageByUuid(params("parent"))
		val page = new Page(params("title"), params("shortlink"), params("author"))
		parent.addPage(page)
		pageRepository.save(page)
		redirect(uri("/fink-admin/pages"))
	}

	get("/pages/edit/:uuid") {
		val page = pageRepository.findPageByUuid(params("uuid"))
		val rootPage = pageRepository.find("title", "Website")

		templateAttributes("page") = page
		templateAttributes("rootPage") = rootPage
		templateAttributes("pageJson") = generate(page)

		layout("pages.edit")
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
		page.setShortlink(params("shortlink"))
		page.setAuthor(params("author"))
		page.setText(params("text"))

		pageRepository.save(page)
		pageRepository.save(parent)

		redirect(uri("/fink-admin/pages"))
	}

	get("/posts") {
		templateAttributes("posts") = postRepository.getEntries
		layout("posts.index")
	}

	get("/posts/create") {
		layout("posts.create")
	}

	post("/posts/create") {
		postRepository.createPost(params("title"), params("text"), params("author"), params("category"), params("tags"))
		redirect(uri("/fink-admin/posts"))
	}

	get("/posts/edit/:uuid") {
		val post = postRepository.findPostByUuid(params("uuid"))
		templateAttributes("post") = post
		layout("posts.edit")
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

		redirect(uri("/fink-admin/posts"))
	}

	get("/collections") {
		templateAttributes("collections") = mediaRepository.findAll
		layout("collections.index")
	}

	get("/collections/create") {
		render("collections.create")
	}

	get("/collections/list") {
		render("collections.list")
	}

	post("/collections/create") {
		val c = mediaRepository.createCollection(params("title"))
		c.setShortlink(params("shortlink"))
		mediaRepository.save(c)
		c.getUuid
	}

	get("/collections/edit/:id") {
		val c = mediaRepository.findCollection(params("id"))
		templateAttributes("collection") = c
		render("collections.edit")
	}

	get("/collections/edit/:id/images") {
		val c = mediaRepository.findCollection(params("id"))
		templateAttributes("collection") = c
		render("collections.images")
	}

	post("/collections/edit/:id/update") {
		val title = params("title")
		val author = params("author")
		val tags = params("tags")
		val shortlink = params("shortlink")

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
		c.setShortlink(shortlink)
		mediaRepository.save(c)
	}

	post("/collections/edit/:id/sort") {
		params("order") match {
			case s : String => {
				val order = s.split(",")
				mediaRepository.findCollection(params("id")) match {
					case c : MediaCollection => {
						mediaRepository.sortImages(c, order)
						mediaRepository.save(c)
					}
				}
			}
		}
	}

	post("/collections/edit/:id/setcover") {
		val collection = mediaRepository.findCollection(params("id"))
		val image = imageRepository.findImage(params("mediaid"))
		collection.setCover(image)
		mediaRepository.save(collection)
	}

	post("/collections/edit/:id/add") {
		val collection = mediaRepository.findCollection(params("id"))
		val image = MediaManager.processUpload(fileParams("file"))
		collection.addItem(image)
		mediaRepository.save(collection)
		//redirect(uri("/fink-admin/collections/edit/" + collection.getUuid))
	}

	post("/collections/edit/:id/delete") {
		"ok"
	}

	get("/images") {
		templateAttributes("images") = MediaManager.getImagesList
		layout("admin.images")
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
		templateAttributes("images") = MediaManager.getImagesList
		render("images")
	}

	get("/views/shared/:id") {
		<div class="entry" data-mixin="pageTree">
			<div class="info">
				<span class="control folder-open"></span>
				<span class="title"><a data-bind="page.title" data-bind-href="page.uuid"></a></span>
				<div class="clear"></div>
			</div>
			<div class="entries">
	  	<div data-foreach-page="page.subPages">
				<div data-bind-page="page" />
				<div data-partial="shared/pages-tree"></div>
			</div>
			</div>
		</div>
	}

	notFound {
		<h1>Not found.  Bummer.</h1>
	}

	protected def contextPath = request.getContextPath

}
