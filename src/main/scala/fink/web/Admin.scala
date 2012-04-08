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

import org.scalatra.fileupload.FileUploadSupport
import org.scalatra.scalate.ScalateSupport
import org.scalatra.ScalatraServlet
import org.scalatra.servlet._

import com.codahale.jerkson.Json.generate
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

class Admin extends ScalatraServlet with RepositorySupport with ResourcesSupport with AuthenticationRoutes with ScalateSupport with FileUploadSupport {

	// implicit val formats = Serialization.formats(NoTypeHints)
	implicit override val jsonFormats = Serialization.formats(ShortTypeHints(List(classOf[Post], classOf[Page])))

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

	get("/bla") {
		"asdojaosdj"
	}

	get("/") {
		// layout("admin.index")
		templateAttributes("layout") = (adminTemplateBase + "/layouts/coffee.jade")
		jade(adminTemplateBase + "/index.jade")	
	}

	get("/coffee") {
		templateAttributes("layout") = (adminTemplateBase + "/layouts/coffee.jade")
		jade(adminTemplateBase + "/index.jade")	
	}

	get("/configuration") {
		layout("admin.config")
	}

	get("/pages") {
		val root = pageRepository.find("title", "Website")
		templateAttributes("rootPage") = root
		layout("pages.index")
	}

	get("/pages/create") {
		templateAttributes("rootPage") = pageRepository.find("title", "Website")
		layout("pages.create")
	}

	post("/pages/create") {
		//pageRepository.createPage(params("title"), params("shortlink"), params("author"), params("parent"))
		val parent = pageRepository.findPageByUuid(params("parent"))
		pageRepository.createPage(params("title"), params("shortlink"), params("author"), parent)
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
	  pageRepository.updatePage(params("uuid"), params("parent"), params("title"), params("shortlink"), params("author"), params("text"), params("tags").split(",").toList)
		redirect(uri("/fink-admin/pages"))
	}

	get("/posts") {
		templateAttributes("posts") = postRepository.findAll
		layout("posts.index")
	}

	get("/posts/create") {
		layout("posts.create")
	}

	post("/posts/create") {
		// TODO process category
		//params("category")
		postRepository.createPost(params("title"), params("text"), params("author"), null, params("tags"))
		redirect(uri("/fink-admin/posts"))
	}

	get("/posts/edit/:uuid") {
		val post = postRepository.findPostByUuid(params("uuid"))
		templateAttributes("post") = post
		layout("posts.edit")
	}

	post("/posts/edit/:uuid") {
		for {
			post <- postRepository.findPostByUuid(params("uuid"))
		} yield {
			// post.clearTags

			// params("tags").split(",").foreach{ t =>
			// 	var tag = tagRepository.findTag(t).getOrElse(tagRepository.createTag(t))
			// 	post.addTag(tag)
			// }

			// post.setTitle(params("title"))
			// post.setAuthor(params("title"))
			// post.setText(params("text"))

			// val postCopy = post.copy(title = params("title"), author = params("author"), text = params("text"))
			val postCopy = post.copy(title = params("title"), author = params("author"))
			postRepository.save(postCopy)
		}

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
		val collection = mediaRepository.createCollection(params("title"))
		val collectionCopy = collection.copy(shortlink = params("shortlink"))
		mediaRepository.save(collectionCopy)
		collectionCopy.id
	}

	get("/collections/edit/:id") {
		mediaRepository.findCollection(params("id")) match {
			case Some(c) =>
				templateAttributes("collection") = c
				render("collections.edit")
			case None =>
		}
	}

	get("/collections/edit/:id/images") {
		mediaRepository.findCollection(params("id")) match {
			case Some(c) =>
				templateAttributes("collection") = c
				render("collections.images")
			case None =>
		}
	}

	get("/collections/edit/:id/image/:imageid") {
		mediaRepository.byId(JLong.parseLong(params("imageid"))) match {
			case Some(image) => 
				templateAttributes("image") = image
				render("collections.image")
			case None =>
		}
	}
	
	post("/collections/edit/:id/image/:imageid/delete") {
		for {
			collection <- mediaRepository.findCollection(params("id"))
			image <- imageRepository.byId(JLong.parseLong(params("imageid")))
		} yield {
			collection.removeItem(image)
			mediaRepository.unlinkImage(collection, image)
			mediaRepository.deleteImage(collection, image)
		}
		// val collection = mediaRepository.findCollection(params("id"))
		// val image = mediaRepository.byId(params("imageid"))
		true
	}

	post("/collections/edit/:id/image/:imageid") {
		for {
			image <- imageRepository.byId(JLong.parseLong(params("imageid")))
			title <- Option(params("title"))
		} yield {
			val imageCopy = image.copy(title = title)
			mediaRepository.saveImage(image)
		}
		true
	}

	post("/collections/edit/:id/update") {
		val title = params("title")
		val author = params("author")
		val tags = params("tags")
		val shortlink = params("shortlink")

		mediaRepository.findCollection(params("id")) match {
			case Some(collection) =>
				tags.split(",").foreach { t=>
					var tag = tagRepository.findTag(t).getOrElse(tagRepository.createTag(t))
					collection.addTag(tag)
				}

				//val collectionCopy = collection.copy(title = title, author = author, shortlink = "")
				val collectionCopy = collection.copy(title = title, author = author)
				mediaRepository.save(collection)
			case None =>
		}
	}

	post("/collections/edit/:id/sort") {
		// params("order") match {
		// 	case s : String => {
		// 		val order = s.split(",")
		// 		mediaRepository.findCollection(params("id")) match {
		// 			case c : MediaCollection => {
		// 				mediaRepository.sortImages(c, order)
		// 				mediaRepository.save(c)
		// 			}
		// 		}
		// 	}
		// }

		for {
			order <- Option(params("order"))
			orders <- Option(order.split(",").toList)
			collection <- mediaRepository.findCollection(params("id"))
		} yield {
			mediaRepository.sortImages(collection, orders)
			mediaRepository.save(collection)
		}
	}

	post("/collections/edit/:id/setcover") {
		for {
			collection <- mediaRepository.findCollection(params("id"))
			image <- imageRepository.byId(JLong.parseLong(params("mediaid")))
		} yield {
			collection.setCover(image)
			mediaRepository.save(collection)
		}
	}

	post("/collections/edit/:id/add") {
		for {
			collection <- mediaRepository.findCollection(params("id"))
			image <- MediaManager.processUpload(fileParams("file"))
		} yield {
			collection.addItem(image)
			mediaRepository.save(collection)
		}
	}

	post("/collections/edit/:id/delete") {
		"ok"
	}

  get("/rest/collections") {
    contentType = "application/json"
    generate(mediaRepository.findCollections())
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

}
