package org.noorg.fink.admin.web

import org.joda.time.format.DateTimeFormat
import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport
import scala.collection.JavaConversions._
import org.noorg.fink.data.repository.PostRepository

class Frontend extends ScalatraServlet with ScalateSupport {

	def layout(template: String, attributes: Map[String, Any]) = templateEngine.layout("/WEB-INF/" + template + ".scaml", attributes)

	def layout(template: String) = templateEngine.layout("/WEB-INF/" + template + ".scaml")

	val repository = new PostRepository()
	
	before {
		contentType = "text/html"
	}

	get("/") {
		templateEngine.layout("/WEB-INF/index.scaml", Map("content" -> "Hello World"))
	}

	get("/post/:year/:month/:day/:title") {
		val year = params("year").toInt
		val month = params("month").toInt
		val day = params("day").toInt
		val title = params("title")
		val post = repository.findPost(year, month, day, title)
		
		val fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
		
		layout("post", Map("post" -> post, "date" -> fmt.print(post.getDate())))
	}
	
	get("/about") {
		templateEngine.layout("/WEB-INF/about.scaml", Map("content" -> "Hello World"))
	}

	get("/read") {
		val repository = new PostRepository()
		layout("read", Map("content" -> repository.getEntries()))
	}

	get("/dates") {
		val repository = new PostRepository()
		templateEngine.layout("/WEB-INF/dates.scaml", Map("content" -> repository.getEntries()))
	}

	protected def contextPath = request.getContextPath

}