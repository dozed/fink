package fink.web

import fink.data._
import fink.support._

import org.scalatra.servlet.FileUploadSupport
import org.scalatra.ScalatraServlet
import org.scalatra.json.{JacksonJsonSupport, JValueResult}

import org.json4s.jackson.Serialization.read

import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession

trait ResourceRoutes extends ScalatraServlet with RepositorySupport with FileUploadSupport with JacksonJsonSupport {

  get("/api/settings") {
    db withSession {
      Query(SettingsTable).firstOption match {
        case Some(s) => s
        case None => halt(500, "Internal error.")
      }
    }
  }

  post("/api/settings") {
    db withSession {
      val settings = read[Settings](request.body)
      val updated = Query(SettingsTable).update(settings)
    }
  }

  get("/api/pages") {
    pageRepository.findAll
  }

  post("/api/pages") {
    val page = read[Page](request.body)
    val id = pageRepository.create(page.date, page.title, page.author, page.text, page.tags.map(_.name))

    pageRepository.byId(id) match {
      case Some(page) => page
      case None => halt(500)
    }
  }

  get("/api/pages/:id") {
    val id = params("id").toLong

    pageRepository.byId(id) match {
      case Some(page) => page
      case None => halt(404)
    }
  }

  put("/api/pages/:id") {
    val page = read[Page](request.body)

    pageRepository.update(page) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  delete("/api/pages/:id") {
    val id = params("id").toLong

    pageRepository.delete(id) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404)
    }
  }

  get("/api/posts") {
    postRepository.findAll
  }

  post("/api/posts") {
    val post = read[Post](request.body)
    val id = postRepository.create(post.date, post.title, post.author, post.text, post.tags.map(_.name), post.category)

    postRepository.byId(id) match {
      case Some(post) => post
      case None => halt(500)
    }
  }

  get("/api/posts/:id") {
    val id = params("id").toLong

    postRepository.byId(id) match {
      case Some(post) => post
      case None => halt(404)
    }
  }

  put("/api/posts/:id") {
    val post = read[Post](request.body)

    postRepository.update(post) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  delete("/api/posts/:id") {
    val id = params("id").toLong

    postRepository.delete(id) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404)
    }
  }

  get("/api/posts/:id/tags") {
    val id = params("id").toLong

    postRepository.byId(id) match {
      case Some(post) => post.tags
      case None => halt(404)
    }
  }

  post("/api/posts/:id/tags/:name") {
    val id = params("id").toLong
    val name = params("name")

    postRepository.addTag(id, name) match {
      case Ok => halt(204)
      case AlreadyExists => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  delete("/api/posts/:id/tags/:name") {
    val id = params("id").toLong
    val name = params("name")

    postRepository.removeTag(id, name) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  get("/api/posts/:id/category") {
    val id = params("id").toLong

    postRepository.byId(id) match {
      case Some(post) => post.category.getOrElse(halt(404))
      case None => halt(404)
    }
  }

  put("/api/posts/:id/category/:name") {
    val id = params("id").toLong
    val name = params("name")

    // alternative: retrieve post, retrieve category, clone post with new category, update post
    postRepository.modifyCategory(id, name) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  get("/api/categories") {
    categoryRepository.findAll
  }

  get("/api/categories/:id") {
    val id = params("id").toLong

    categoryRepository.byId(id) match {
      case Some(category) => category
      case None => halt(404)
    }
  }

  post("/api/categories") {
    val category = read[Category](request.body)
    val id = categoryRepository.create(category.name) // unique constraint

    categoryRepository.byId(id) match {
      case Some(category) => category
      case None => halt(500)
    }
  }

  put("/api/categories/:id") {
    val category = read[Category](request.body)
    val id = params("id").toLong

    categoryRepository.update(category) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  delete("/api/categories/:id") {
    val id = params("id").toLong

    categoryRepository.delete(id) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  get("/api/tags") {
    tagRepository.findAll
  }

  get("/api/tags/:id") {
    val id = params("id").toLong

    tagRepository.byId(id) match {
      case Some(tag) => tag
      case None => halt(404)
    }
  }

  post("/api/tags") {
    val tag = read[Tag](request.body)
    val id = tagRepository.create(tag.name) // unique constraint

    tagRepository.byId(id) match {
      case Some(tag) => tag
      case None => halt(500)
    }
  }

  put("/api/tags/:id") {
    val tag = read[Tag](request.body)
    val id = params("id").toLong

    tagRepository.update(tag) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  delete("/api/tags/:id") {
    val id = params("id").toLong

    tagRepository.delete(id) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  get("/api/galleries") {
    galleryRepository.findAll
  }

  post("/api/galleries") {
    val gallery = read[Gallery](request.body)
    val id = galleryRepository.create(gallery.coverId, gallery.date, gallery.title, gallery.author, gallery.shortlink, gallery.text, gallery.tags.map(_.name))

    galleryRepository.byId(id) match {
      case Some(gallery) => gallery
      case None => halt(500)
    }
  }

  get("/api/galleries/:id") {
    val id = params("id").toLong

    galleryRepository.byId(id) match {
      case Some(gallery) => gallery
      case None => halt(404, "Could not find gallery: %s".format(id))
    }
  }

  put("/api/galleries/:id") {
    val id = params("id").toLong
    val gallery = read[Gallery](request.body)

    galleryRepository.update(gallery) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  delete("/api/galleries/:id") {
    val id = params("id").toLong
    galleryRepository.delete(id) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  get("/api/galleries/:id/cover") {
    val id = params("id").toLong

    galleryRepository.byId(id).map(_.coverId) match {
      case Some(coverId) if coverId != 0 => "cover %s".format(coverId)
      case None => halt(404, "Could not find cover for gallery: %s".format(id))
    }
  }

  post("/api/galleries/:id/cover") {
    val id = params("id").toLong
    val coverId = params("coverId").toLong
    galleryRepository.setCover(id, coverId)
    halt(204)
  }

  get("/api/galleries/:id/images") {
    val id = params("id").toLong

    galleryRepository.byId(id) match {
      case Some(gallery) => gallery.images
      case None => halt(404, "Could not find gallery: %s".format(id))
    }
  }

  post("/api/galleries/:id/images") {
    val id = params("id").toLong
    val order = params("order").split(",").toList.map(_.toLong)

    galleryRepository.updateImageOrder(id, order)
    halt(204)
  }

  post("/api/galleries/:galleryId/images/:imageId") {
    val galleryId = params("galleryId").toLong
    // val imageId = params("imageId").toLong => 500
    val imageId = params("imageId").toLong

    galleryRepository.addImage(galleryId, imageId) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  delete("/api/galleries/:galleryId/images/:imageId") {
    val galleryId = params("galleryId").toLong
    val imageId = params("imageId").toLong

    galleryRepository.removeImage(galleryId, imageId) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  get("/api/images") {
    imageRepository.findAll
  }

  post("/api/images") {
    MediaManager.processUpload(fileParams("file")) match {
      case Some(ImageUpload(hash, contentType, filename)) =>
        imageRepository.create(0, filename, "", hash, contentType, filename)
      case None => halt(500)
    }
  }

  get("/api/images/:id") {
    val id = params("id").toLong

    imageRepository.byId(id) match {
      case Some(image) => image
      case None => halt(404, "Could not find image: %s".format(id))
    }
  }

  put("/api/images/:id") {
    val id = params("id").toLong
    val image = read[Image](request.body)

    imageRepository.update(image) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  delete("/api/images/:id") {
    val id = params("id").toLong
    imageRepository.delete(id) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

}
