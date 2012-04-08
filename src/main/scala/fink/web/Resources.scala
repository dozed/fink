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

import org.scalatra.liftjson.LiftJsonRequestBodyWithoutFormats
import org.scalatra.fileupload.FileUploadSupport
import org.scalatra.scalate.ScalateSupport
import org.scalatra.ScalatraServlet
import org.scalatra.servlet._

import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

trait ResourcesSupport extends ScalatraServlet with RepositorySupport with LiftJsonRequestBodyWithoutFormats {

	// implicit val formats = Serialization.formats(NoTypeHints)
	implicit val jsonFormats = Serialization.formats(ShortTypeHints(List(classOf[Post], classOf[Page])))

	get("/api/posts") {
		write(postRepository.findAll)
	}

	post("/api/posts") {
		val page = postRepository.save(Post(title=params("title"), text=params("text"), author=params("author")))
		write(page)
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

}
