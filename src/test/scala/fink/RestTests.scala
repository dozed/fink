package fink

import org.specs2.mutable._

import scala.collection.JavaConversions._

import fink.data._
import fink.web._

import org.scalatra.test.specs2.MutableScalatraSpec
import org.scalatra.ScalatraServlet

import net.liftweb.json._
import net.liftweb.json.Serialization.{read => jsread, write => jswrite}

class RestTests extends MutableScalatraSpec {

  sequential

  addServlet(classOf[fink.web.Admin], "/*")

  implicit val jsonFormats = Serialization.formats(ShortTypeHints(List(classOf[Page], classOf[Category], classOf[Tag]))) + FieldSerializer[Post]()  + FieldSerializer[Gallery]() + FieldSerializer[Image]()

  def dummyPost = {
    val p = Post(0, 0, 1, "title", "author", "text")
    p.tags = List(Tag(0, "foo"), Tag(0, "bar"))
    p.category = Some(Category(0, "cat"))
    p
  }

  "posts" should {
    "should create post" in {
      val p = dummyPost
      val rbody = jswrite(p)

      post("/api/posts", headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = rbody) {
        status must beEqualTo(200)
        val p2 = jsread[Post](body)

        (p2.title, p2.author, p2.text) must beEqualTo((p.title, p.author, p.text))
        p2.tags.map(_.name) must containAllOf(p.tags.map(_.name))
        p2.category.map(_.name) must beEqualTo(p.category.map(_.name))
      }
    }

    "should delete post" in {
      val p = dummyPost
      val rbody = jswrite(p)
      post("/api/posts", headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = rbody) {
        status must beEqualTo(200)
        val p2 = jsread[Post](body)

        get("/api/posts") {
          status must beEqualTo(200)
          val posts = jsread[List[Post]](body)

          submit("DELETE", "/api/posts/%s".format(posts.last.id)) {
            status must beEqualTo(204)
          }

          submit("DELETE", "/api/posts/%s".format(posts.last.id)) {
            status must beEqualTo(404)
          }

          get("/api/posts") {
            status must beEqualTo(200)
            val posts2 = jsread[List[Post]](body)
            posts2 must have size(posts.size-1)
          }
        }

        true
      }
    }

    "should find post" in {
      var posts = List[Post]()
      get("/api/posts") {
        status must beEqualTo(200)
        posts = jsread[List[Post]](body)
        posts must have size(1)
      }
    }

    "should find single post" in {
      get("/api/posts/%s".format(1)) {
        status must beEqualTo(200)
        val post = jsread[Post](body)
        true
      }
    }

    "should update post" in {
      val p = Post(1, 0, 1, "title", "author", "text")

      post("/api/posts", headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = jswrite(p)) {
        status must beEqualTo(200)
      }

      val p2 = p.copy(title = "foo bla", author = "foo author", text = "foo text")

      put("/api/posts/%s".format(1), headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = jswrite(p2)) {
        status must beEqualTo(204)
      }

      get("/api/posts/%s".format(1)) {
        status must beEqualTo(200)
        jsread[Post](body) must beEqualTo(p2)
      }
    }

    "should find tags for post" in {
      get("/api/posts/%s/tags".format(1)) {
        status must beEqualTo(200)
        val tags = jsread[List[Tag]](body)
        tags must have size(2)
      }
    }
  }

  "should create new relation from post to tag" in {
    post("/api/posts/%s/tags/%s".format(1, "fnord")) {
      status must beEqualTo(204)
    }

    post("/api/posts/%s/tags/%s".format(1, "fnord")) {
      status must beEqualTo(204)
    }

    get("/api/posts/%s/tags".format(1)) {
      status must beEqualTo(200)
      val tags = jsread[List[Tag]](body)
      tags must have size(3)
    }
  }

  "should delete tag from post" in {
    submit("DELETE", "/api/posts/%s/tags/%s".format(1, "blubbb")) {
      status must beEqualTo(204)
    }

    get("/api/posts/%s/tags".format(1)) {
      status must beEqualTo(200)
      val tags = jsread[List[Tag]](body)
      tags must have size(3)
    }

    // delete("/api/posts/%s/tags/%s".format(1, "fnord")) {
    submit("DELETE", "/api/posts/%s/tags/%s".format(1, "fnord")) {
      status must beEqualTo(204)
    }

    get("/api/posts/%s/tags".format(1)) {
      status must beEqualTo(200)
      val tags = jsread[List[Tag]](body)
      tags must have size(2)
    }
  }

  "should create category" in {
    val rbody = jswrite(Category(0, "foo"))
    post("/api/categories", headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = rbody) {
      status must beEqualTo(200)
    }
  }

  "should retrieve post category" in {
    get("/api/posts/%s/category".format(1)) {
      status must beEqualTo(200)
      val category = jsread[Category](body)
      category must beEqualTo(Category(1, "cat"))
    }
  }

  "should be able to modify post category" in {
    put("/api/posts/%s/category/%s".format(1, "foo")) {
      status must beEqualTo(204)
    }


    get("/api/posts/%s".format(1)) {
      status must beEqualTo(200)
      val post = jsread[Post](body)
      post.category must beSome.which(_.name must beEqualTo("foo"))
    }
  }

  "should do crud on tags" in {
    get("/api/tags") {
      status must beEqualTo(200)
      val tags = jsread[List[Tag]](body)
      val tag = Tag(0, "baz")

      // create tag
      post("/api/tags", headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = jswrite(tag)) {
        status must beEqualTo(200)
        val tag = jsread[Tag](body)
        tag.name must beEqualTo("baz")

        val tag2 = tag.copy(name = "blubbb")

        // modify tag
        put("/api/tags/%s".format(tag2.id), headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = jswrite(tag2)) {
          status must beEqualTo(204)
        }

        get("/api/tags/%s".format(tag2.id)) {
          status must beEqualTo(200)
          val tag3 = jsread[Tag](body)
          tag3.name must beEqualTo("blubbb")
        }

        get("/api/tags") {
          status must beEqualTo(200)
          val tags2 = jsread[List[Tag]](body)
          tags2 must have size(tags.size+1)
        }

        // delete tag
        submit("DELETE", "/api/tags/%s".format(tag2.id)) {
          status must beEqualTo(204)
        }

        submit("DELETE", "/api/tags/%s".format(tag2.id)) {
          status must beEqualTo(404)
        }

        get("/api/tags") {
          status must beEqualTo(200)
          val tags2 = jsread[List[Tag]](body)
          tags2 must have size(tags.size)
        }
      }
    }
  }

  "should do crud on categories" in {
    get("/api/categories") {
      status must beEqualTo(200)
      val categories = jsread[List[Category]](body)
      val category = Category(0, "baz")

      // create category
      post("/api/categories", headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = jswrite(category)) {
        status must beEqualTo(200)
        val category = jsread[Category](body)
        category.name must beEqualTo("baz")

        val category2 = category.copy(name = "blubbb")

        // modify category
        put("/api/categories/%s".format(category2.id), headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = jswrite(category2)) {
          status must beEqualTo(204)
        }

        get("/api/categories/%s".format(category2.id)) {
          status must beEqualTo(200)
          val category3 = jsread[Category](body)
          category3.name must beEqualTo("blubbb")
        }

        get("/api/categories") {
          status must beEqualTo(200)
          val categories2 = jsread[List[Category]](body)
          categories2 must have size(categories.size+1)
        }

        // delete category
        submit("DELETE", "/api/categories/%s".format(category2.id)) {
          status must beEqualTo(204)
        }

        submit("DELETE", "/api/categories/%s".format(category2.id)) {
          status must beEqualTo(404)
        }

        get("/api/categories") {
          status must beEqualTo(200)
          val category2 = jsread[List[Category]](body)
          category2 must have size(categories.size)
        }
      }
    }
  }

  "should do crud on galleries" in {
    get("/api/galleries") {
      status must beEqualTo(200)
      val galleries = jsread[List[Gallery]](body)
      val gallery = Gallery(0, 0, 0, "foo gallery", "blubbb", "shortlink", "text")

      // create gallery
      post("/api/galleries", headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = jswrite(gallery)) {
        status must beEqualTo(200)
        val gallery = jsread[Gallery](body)
        gallery.title must beEqualTo("foo gallery")

        val gallery2 = gallery.copy(title = "blubbb gallery")

        // modify gallery
        put("/api/galleries/%s".format(gallery2.id), headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = jswrite(gallery2)) {
          status must beEqualTo(204)
        }

        get("/api/galleries/%s".format(gallery2.id)) {
          status must beEqualTo(200)
          val gallery3 = jsread[Gallery](body)
          gallery3.title must beEqualTo("blubbb gallery")
        }

        get("/api/galleries") {
          status must beEqualTo(200)
          val galleries2 = jsread[List[Gallery]](body)
          galleries2 must have size(galleries.size+1)
        }

        // delete gallery
        submit("DELETE", "/api/galleries/%s".format(gallery2.id)) {
          status must beEqualTo(204)
        }

        submit("DELETE", "/api/galleries/%s".format(gallery2.id)) {
          status must beEqualTo(404)
        }

        get("/api/galleries") {
          status must beEqualTo(200)
          val gallery2 = jsread[List[Gallery]](body)
          gallery2 must have size(galleries.size)
        }
      }
    }
  } 

  "should do crud on images" in {
    get("/api/images") {
      status must beEqualTo(200)
      val images = jsread[List[Image]](body)
      val image = Image(0, 0, "foo image", "blubbb", "text")

      // create image
      post("/api/images", headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = jswrite(image)) {
        status must beEqualTo(200)
        val image = jsread[Image](body)
        image.title must beEqualTo("foo image")

        val image2 = image.copy(title = "blubbb image")

        // modify image
        put("/api/images/%s".format(image2.id), headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = jswrite(image2)) {
          status must beEqualTo(204)
        }

        get("/api/images/%s".format(image2.id)) {
          status must beEqualTo(200)
          jsread[Image](body).title must beEqualTo("blubbb image")
        }

        get("/api/images") {
          status must beEqualTo(200)
          jsread[List[Image]](body) must have size(images.size+1)
        }

        // delete image
        submit("DELETE", "/api/images/%s".format(image2.id)) {
          status must beEqualTo(204)
        }

        submit("DELETE", "/api/images/%s".format(image2.id)) {
          status must beEqualTo(404)
        }

        get("/api/images") {
          status must beEqualTo(200)
          val image2 = jsread[List[Image]](body)
          image2 must have size(images.size)
        }
      }
    }
  }

  "images can be added and removed to/from galleries" in {
      val gallery = Gallery(0, 0, 0, "foo gallery", "blubbb", "shortlink", "text")
      val image = Image(0, 0, "foo image", "blubbb", "text")

      post("/api/galleries", headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = jswrite(gallery)) {
        status must beEqualTo(200)
        val gallery = jsread[Gallery](body)

        post("/api/images", headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = jswrite(image)) {
          status must beEqualTo(200)
          val image = jsread[Image](body)

          get("/api/galleries/%s/images".format(gallery.id)) {
            status must beEqualTo(200)
            jsread[List[Image]](body) must have size(0)
          }

          post("/api/galleries/%s/images/%s".format(gallery.id, image.id)) {
            status must beEqualTo(204)
          }

          get("/api/galleries/%s/images".format(gallery.id)) {
            status must beEqualTo(200)
            jsread[List[Image]](body) must have size(1)
          }

          submit("DELETE", "/api/galleries/%s/images/%s".format(gallery.id, image.id)) {
            status must beEqualTo(204)
          }

          submit("DELETE", "/api/galleries/%s/images/%s".format(gallery.id, image.id)) {
            status must beEqualTo(404)
          }

          get("/api/galleries/%s/images".format(gallery.id)) {
            status must beEqualTo(200)
            jsread[List[Image]](body) must have size(0)
          }
        }
      }
  }

}
