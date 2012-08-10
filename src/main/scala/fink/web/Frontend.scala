package fink.web

import scala.collection.JavaConversions._
import java.lang.{Long=>JLong}

import fink.data._
import fink.support._

import org.scalatra.scalate.ScalateSupport
import org.scalatra.ScalatraServlet
import org.scalatra.servlet._

import org.joda.time.format.DateTimeFormat
import java.io.File

class Frontend extends ScalatraServlet with ScalateSupport with RepositorySupport {

  def templateBase = "/WEB-INF/frontend"
  
  def layout(template: String) = {
    templateAttributes("layout") = (templateBase + "/layouts/default.jade")
    templateAttributes("galleryRepository") = galleryRepository
    templateAttributes("posts") = postRepository.findAll
    jade("%s/%s.jade".format(templateBase, template))
  }
  
  def uri(uri: String) = {
    if (uri.startsWith("/")) {
      request.getContextPath + uri
    } else {
      uri
    }
  }
 
  before() {
    contentType = "text/html"
  }

  get("/") {
    layout("index")
  }

  get("/post/:year/:month/:day/:title") {
    // val year = params("year").toInt
    // val month = params("month").toInt
    // val day = params("day").toInt
    // val title = params("title")
    
    // postRepository.findPost(year, month, day, title) match {
    //  case Some(post) =>
    //    val fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
    //    templateAttributes("post") = post
    //    templateAttributes("date") = fmt.print(post.date)
    //    layout("post")
    //  case None =>
    // }
  }

  get("/posts/:title") {
    val title = params("title")
    
    postRepository.byTitle(title) match {
      case Some(post) =>
        templateAttributes("post") = post
        // val fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        // templateAttributes("date") = fmt.print(post.date)
        layout("post")
      case None => halt(404, "Not found.")
    }
  }

  get("/pages/:shortlink") {
    val shortlink = params("shortlink")

    pageRepository.byShortlink(shortlink) match {
      case Some(page) =>
        templateAttributes("page") = page
        layout("page")
      case None => halt(404, "Not found.")
    }
  }

  get("/media/:shortlink") {
    galleryRepository.byShortlink(params("shortlink")) match {
      case Some(gallery) =>
        templateAttributes("gallery") = gallery
        layout("album")
      case None => halt(404, "Not found.")
    }
  }

  get("/uploads/images/:hash/:spec/:file") {
    (for {
      hash <- Option(params("hash"))
      image <- imageRepository.byHash(hash)
      ext <- MediaManager.imageExtensions.get(image.contentType)
      spec <- MediaManager.imageSpecs.filter(_.name == params("spec")).headOption
    } yield {
      val file = new File("%s/%s-%s.%s".format(Config.mediaDirectory, image.hash, spec.name, ext))
      if (!file.exists) halt(404)
      response.addHeader("Content-Disposition", "inline;filename=\"%s\"".format(image.filename))
      response.addHeader("Content-type", image.contentType)
      file
    }) getOrElse(halt(404))
  }

  notFound {
    halt(404, "Not found.")
  }

}
