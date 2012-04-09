package fink

import scala.collection.JavaConversions._

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
import fink.data.ContentItemRepository
import fink.web.Admin

import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.PropertyContainer

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import org.scalatest.BeforeAndAfterAll

import org.scalatra.test.scalatest.ScalatraFunSuite
import org.scalatra.ScalatraServlet

import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

class RestTests extends ScalatraFunSuite {

	val imageRepository = new ImageRepository
	val tagRepository = new TagRepository
	val postRepository = new PostRepository
	val pageRepository = new PageRepository
	val mediaRepository = new MediaRepository

	import ContentItemRepository._
	// implicit val formats = Serialization.formats(NoTypeHints)
	implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[Page], classOf[Post])))

	addServlet(classOf[fink.web.Admin], "/*")

	def setupTestData() {
		// withTx { implicit neo =>
		// 	for ( i <- 1 to 10) postRepository.save(Post(0L, null, title = "title", text = "text", author = "author"))
		// }
	}

	override def beforeAll() {
		super.beforeAll
		ContentItemRepository.clear()
		setupTestData()

	}

	override def afterAll() {
		super.afterAll
		ContentItemRepository.shutdown()
	}

	test("should create post via REST") {
		val rbody = write(Post(0L, 0L, title = "title", text = "text", author = "author"))
		post("/api/posts", headers = Map("Accept" -> "application/json", "Content-Type" -> "application/json"), body = rbody) {
			status should equal (200)
		}
	}

	test("should create post via POST") {
		post("/api/posts", "title" -> "a title", "text" -> "some cool text" , "author" -> "boom wah this") {
			status should equal (200)
		}
	}

	test("should find post") {
		var posts = List[Post]()
		get("/api/posts") {
			status should equal (200)
			posts = read[List[Post]](body)
			assert(posts.size == 2)
		}

		get("/api/posts/%s".format(posts(0).id)) {
			status should equal (200)
			val post = read[Post](body)
		}
	}

	test("should create page") {
		post("/api/pages", "title" -> "a title", "shortlink" -> "a short link" , "author" -> "boom wah this") {
			status should equal (200)
			val page = read[Page](body)
		}
	}

	test("should find page") {
		var pages = List[Page]()

		get("/api/pages") {
			status should equal (200)
			pages = read[List[Page]](body)
			assert(pages.size == 1)
		}

		get("/api/pages/find/%s".format(pages(0).id)) {
			status should equal (200)
			read[Page](body)
		}
	}
}
