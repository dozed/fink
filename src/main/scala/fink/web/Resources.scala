package fink.web

import scala.collection.JavaConversions._
import java.lang.{Long=>JLong}

import fink.data._
import fink.support._

import org.scalatra.liftjson.LiftJsonRequestBodyWithoutFormats

import org.scalatra.fileupload.FileUploadSupport
import org.scalatra.scalate.ScalateSupport
import org.scalatra.ScalatraServlet
import org.scalatra.servlet._

import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

import org.joda.time.DateTime

trait ResourcesSupport extends ScalatraServlet with RepositorySupport with LiftJsonRequestBodyWithoutFormats {

	implicit val jsonFormats = Serialization.formats(ShortTypeHints(List(classOf[Page], classOf[Category], classOf[Tag]))) + FieldSerializer[Post]()

	get("/api/posts") {
		write(postRepository.findAll)
	}

	post("/api/posts") {

		// support json and http post
		val mt = request.contentType map {
			_.split(";").head
		} getOrElse "application/x-www-form-urlencoded"
		val fmt = mimeTypes get mt getOrElse "html"

		if (fmt.equals("json")) {
			val r = read[Post](request.body)
			val post = postRepository.save(r)
			write(post)
		} else {
			val post = postRepository.save(Post(title=params("title"), text=params("text"), author=params("author"), date=(new DateTime).getMillis))
			write(post)			
		}
	}

	get("/api/posts/:id") {
		val id = JLong.parseLong(params("id"))
		postRepository.byId(id).map(write(_)).getOrElse(write("error"))
	}
 
	put("/api/posts/:id") {
		val id = JLong.parseLong(params("id"))

		if (id == 0) {
			// val post = read[Post](request.body)
			// val created = postRepository.save(post)
			// write(created)
			""
		} else {
			val post = read[Post](request.body)
			val updated = postRepository.update(post)
			write(updated)
		}
	}

	delete("/api/posts/:id") {
		val id = JLong.parseLong(params("id"))
		postRepository.delete(id)
		""
	}

	get("/api/pages") {
		write(pageRepository.findAll)
	}

	post("/api/pages") {
		// pageRepository.createPage(params("title"), params("shortlink"), params("author"), params("parent"))
		// val parent = pageRepository.findPageByUuid(params("parent"))
		val page = pageRepository.save(Page(title=params("title"), shortlink=params("shortlink"), author=params("author")))
		write(page)
	}

	get("/api/pages/find/:id") {
		val id = JLong.parseLong(params("id"))
		pageRepository.byId(id).map(write(_)).getOrElse(write("error"))
	}


	get("/api/galleries") {
		write(galleryRepository.findAll)
	}

	post("/api/galleries") {
		val r = read[Gallery](request.body)
		val gallery = galleryRepository.save(r)
		write(gallery)
	}

	get("/api/galleries/:id") {
		val id = JLong.parseLong(params("id"))
		galleryRepository.byId(id).map(write(_)).getOrElse(write("error"))
	}
 
	put("/api/galleries/:id") {
		val id = JLong.parseLong(params("id"))

		if (id != 0) {
			val gallery = read[Gallery](request.body)
			write(galleryRepository.update(gallery))
		}
	}

	delete("/api/galleries/:id") {
		val id = JLong.parseLong(params("id"))
		galleryRepository.delete(id)
		""
	}


	get("/api/categories") {
		categoryRepository.createIfNotExist(Config.postCategories)
		write(categoryRepository.findAll)
	}

	get("/api/tags") {
		write(tagRepository.findAll)
	}

}
