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
import net.liftweb.json.Serialization.{read => jsread, write => jswrite}

import org.joda.time.DateTime

trait ResourcesSupport extends ScalatraServlet with RepositorySupport with LiftJsonRequestBodyWithoutFormats {

  implicit val jsonFormats = Serialization.formats(ShortTypeHints(List(classOf[Page], classOf[Category], classOf[Tag]))) + FieldSerializer[Post]()  + FieldSerializer[Gallery]() + FieldSerializer[Image]()

  get("/api/posts") {
    jswrite(postRepository.findAll)
  }

  post("/api/posts") {
    val post = jsread[Post](request.body)
    val id = postRepository.create(post)

    postRepository.byId(id) match {
      case Some(post) => jswrite(post)
      case None => halt(500)
    }
  }

  get("/api/posts/:id") {
    val id = JLong.parseLong(params("id"))

    postRepository.byId(id) match {
      case Some(post) => jswrite(post)
      case None => halt(404)
    }
  }

  put("/api/posts/:id") {
    val id = JLong.parseLong(params("id"))
    val post = jsread[Post](request.body)

    postRepository.update(post) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  delete("/api/posts/:id") {
    val id = JLong.parseLong(params("id"))

    postRepository.delete(id) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404)
    }
  }
 
  get("/api/posts/:id/tags") {
    val id = JLong.parseLong(params("id"))

    postRepository.byId(id) match {
      case Some(post) => jswrite(post.tags)
      case None => halt(404)
    }
  }

  post("/api/posts/:id/tags/:name") {
    val id = JLong.parseLong(params("id"))
    val name = params("name")

    postRepository.addTag(id, name) match {
      case Ok => halt(204)
      case AlreadyExists => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  delete("/api/posts/:id/tags/:name") {
    val id = JLong.parseLong(params("id"))
    val name = params("name")

    postRepository.removeTag(id, name) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  get("/api/posts/:id/category") {
    val id = JLong.parseLong(params("id"))
    
    postRepository.byId(id) match {
      case Some(post) => post.category.map(jswrite(_)).getOrElse(halt(404))
      case None => halt(404)
    }
  }

  put("/api/posts/:id/category/:name") {
    val id = JLong.parseLong(params("id"))
    val name = params("name")
    
    // alternative: retrieve post, retrieve category, clone post with new category, update post
    postRepository.modifyCategory(id, name) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  get("/api/categories") {
    jswrite(categoryRepository.findAll)
  }

  get("/api/categories/:id") {
    val id = JLong.parseLong(params("id"))
    
    categoryRepository.byId(id) match {
      case Some(category) => jswrite(category)
      case None => halt(404)
    }
  }

  post("/api/categories") {
    val category = jsread[Category](request.body)
    val id = categoryRepository.create(category.name) // unique constraint
    
    categoryRepository.byId(id) match {
      case Some(category) => jswrite(category)
      case None => halt(500)
    }
  }

  put("/api/categories/:id") {
    val category = jsread[Category](request.body)
    val id = JLong.parseLong(params("id"))

    categoryRepository.update(category) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  delete("/api/categories/:id") {
    val id = JLong.parseLong(params("id"))

    categoryRepository.delete(id) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  get("/api/tags") {
    jswrite(tagRepository.findAll)
  }

  get("/api/tags/:id") {
    val id = JLong.parseLong(params("id"))
    
    tagRepository.byId(id) match {
      case Some(tag) => jswrite(tag)
      case None => halt(404)
    }
  }

  post("/api/tags") {
    val tag = jsread[Tag](request.body)
    val id = tagRepository.create(tag.name) // unique constraint
    
    tagRepository.byId(id) match {
      case Some(tag) => jswrite(tag)
      case None => halt(500)
    }
  }

  put("/api/tags/:id") {
    val tag = jsread[Tag](request.body)
    val id = JLong.parseLong(params("id"))

    tagRepository.update(tag) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  delete("/api/tags/:id") {
    val id = JLong.parseLong(params("id"))

    tagRepository.delete(id) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  get("/api/galleries") {
    jswrite(galleryRepository.findAll)
  }

  post("/api/galleries") {
    val gallery = jsread[Gallery](request.body)
    val id = galleryRepository.create(gallery.coverId, gallery.date, gallery.title, gallery.author, gallery.shortlink, gallery.text)

    galleryRepository.byId(id) match {
      case Some(gallery) => jswrite(gallery)
      case None => halt(500)
    }
  }

  get("/api/galleries/:id") {
    val id = JLong.parseLong(params("id"))

    galleryRepository.byId(id) match {
      case Some(gallery) => jswrite(gallery)
      case None => halt(404, "Could not find gallery: %s".format(id))
    }
  }
 
  put("/api/galleries/:id") {
    val id = JLong.parseLong(params("id"))
    val gallery = jsread[Gallery](request.body)

    galleryRepository.update(gallery) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  delete("/api/galleries/:id") {
    val id = JLong.parseLong(params("id"))
    galleryRepository.delete(id) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  get("/api/galleries/:id/images") {
    val id = JLong.parseLong(params("id"))

    galleryRepository.byId(id) match {
      case Some(gallery) => jswrite(gallery.images)
      case None => halt(404, "Could not find gallery: %s".format(id))
    }
  }

  post("/api/galleries/:galleryId/images/:imageId") {
    val galleryId = JLong.parseLong(params("galleryId"))
    // val imageId = JLong.parseLong(params("iamgeId")) => 500
    val imageId = JLong.parseLong(params("imageId"))

    galleryRepository.addImage(galleryId, imageId) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }
 
  delete("/api/galleries/:galleryId/images/:imageId") {
    val galleryId = JLong.parseLong(params("galleryId"))
    val imageId = JLong.parseLong(params("imageId"))

    galleryRepository.removeImage(galleryId, imageId) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  get("/api/images") {
    jswrite(imageRepository.findAll)
  }

  post("/api/images") {
    val image = jsread[Image](request.body)
    val id = imageRepository.create(image.date, image.title, image.author, image.hash)

    imageRepository.byId(id) match {
      case Some(gallery) => jswrite(gallery)
      case None => halt(500)
    }
  }

  get("/api/images/:id") {
    val id = JLong.parseLong(params("id"))

    imageRepository.byId(id) match {
      case Some(image) => jswrite(image)
      case None => halt(404, "Could not find image: %s".format(id))
    }
  }
 
  put("/api/images/:id") {
    val id = JLong.parseLong(params("id"))
    val image = jsread[Image](request.body)

    imageRepository.update(image) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

  delete("/api/images/:id") {
    val id = JLong.parseLong(params("id"))
    imageRepository.delete(id) match {
      case Ok => halt(204)
      case NotFound(message) => halt(404, message)
    }
  }

}
