package fink.web

import scala.collection.JavaConversions._
import java.lang.{Long=>JLong}

import fink.data._
import fink.support._

// import org.scalatra.liftjson.LiftJsonRequestBodyWithoutFormats
import net.liftweb.json._
import net.liftweb.json.Xml._
import java.io.InputStreamReader
import org.scalatra.util.RicherString._
import org.scalatra.ApiFormats
import org.scalatra.ScalatraBase
import org.scalatra.MatchedRoute
import java.nio.CharBuffer


import org.scalatra.fileupload.FileUploadSupport
import org.scalatra.scalate.ScalateSupport
import org.scalatra.ScalatraServlet
import org.scalatra.servlet._

import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}


object LiftJsonRequestBody {
	val ParsedBodyKey = "org.scalatra.liftjson.ParsedBody".intern
}

trait LiftJsonRequestBodyWithoutFormats extends ScalatraBase with ApiFormats {
	import LiftJsonRequestBody._
	 
	protected def parseRequestBody(format: String) = try {
		if (format == "json") {
			transformRequestBody(JsonParser.parse(new InputStreamReader(request.inputStream)))
		} else if (format == "xml") {
			transformRequestBody(toJson(scala.xml.XML.load(request.inputStream)))
		} else JNothing
	} catch {
		case _ ⇒ JNothing
	}

	protected def transformRequestBody(body: JValue) = body

	override protected def invoke(matchedRoute: MatchedRoute) = {
		withRouteMultiParams(Some(matchedRoute)) {
			val mt = request.contentType map {
				_.split(";").head
			} getOrElse "application/x-www-form-urlencoded"
			val fmt = mimeTypes get mt getOrElse "html"
			if (shouldParseBody(fmt)) {
				request(ParsedBodyKey) = parseRequestBody(fmt)
			}
			super.invoke(matchedRoute)
		}
	}

	private def shouldParseBody(fmt: String) =
		(fmt == "json" || fmt == "xml") && parsedBody == JNothing

	def parsedBody = request.get(ParsedBodyKey) getOrElse JNothing
}

/**
 * Parses request bodies with lift json if the appropriate content type is set.
 * Be aware that it also parses XML and returns a JValue of the parsed XML.
 */
trait LiftJsonRequestBody extends LiftJsonRequestBodyWithoutFormats {
	protected implicit def jsonFormats: Formats = DefaultFormats
}


trait ResourcesSupport extends ScalatraServlet with RepositorySupport with LiftJsonRequestBodyWithoutFormats {

	// implicit val formats = Serialization.formats(NoTypeHints)
	implicit val jsonFormats = Serialization.formats(ShortTypeHints(List(classOf[Post], classOf[Page])))

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
			val post = postRepository.save(read[Post](request.body))
			write(post)
		} else {
			val post = postRepository.save(Post(title=params("title"), text=params("text"), author=params("author")))
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

	get("/api/categories") {
		println(123)
		categoryRepository.createIfNotExist(Config.postCategories)
		write(categoryRepository.findAll)
	}


}
