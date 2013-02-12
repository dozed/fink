package fink.web

import fink.data._

import org.scalatra.scalate.ScalateSupport
import org.scalatra.{ApiFormats, ScalatraServlet}

class Frontend extends ScalatraServlet with ApiFormats with ScalateSupport with RepositorySupport with MediaSupport {

  override def jade(template: String, attributes: (String, Any)*)(implicit request: javax.servlet.http.HttpServletRequest, response: javax.servlet.http.HttpServletResponse) = {
    templateAttributes("layout") = ("/frontend/layouts/default.jade")
    super.jade("/frontend/%s.jade".format(template), attributes:_*)
  }
  
  before() {
    contentType = formats("html")
  }

  get("/") {
    jade("index")
  }

  get("/:year/:month/:day/:shortlink/?") {
    val year = params("year").toInt
    val month = params("month").toInt
    val day = params("day").toInt
    val shortlink = params("shortlink")
    
    // postRepository.byShortlink(year, month, day, shortlink) match {
    postRepository.byShortlink(shortlink) match {
     case Some(post) => jade("post", "post" -> post)
     case None => halt(404, "Not found.")
    }
  }

  get("/pages/:shortlink") {
    val shortlink = params("shortlink")

    pageRepository.byShortlink(shortlink) match {
      case Some(page) => jade("page", "page" -> page)
      case None => halt(404, "Not found.")
    }
  }

  get("/media/:shortlink") {
    galleryRepository.byShortlink(params("shortlink")) match {
      case Some(gallery) => jade("album", "gallery" -> gallery)
      case None => halt(404, "Not found.")
    }
  }

  notFound {
    contentType = null
    serveStaticResource() getOrElse halt(404, "Not found.")
  }

}
