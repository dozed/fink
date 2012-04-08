package fink

import scala.collection.JavaConversions._

import fink.support._
import fink.data._

import org.neo4j.graphdb.index._
import org.neo4j.graphdb._

import org.scalatest._

class DataTests extends FunSuite with BeforeAndAfterAll with RepositorySupport {

	import ContentItemRepository._

	override def beforeAll() {
		clear()
		categoryRepository.createIfNotExist(Config.postCategories)
	}

	override def afterAll() {
		shutdown()
	}

	test("should save and findall") {
		val page = pageRepository.save(Page(title="title", shortlink="shortlink", author="author"))
		assert(pageRepository.findAll().toList.size == 1)
	}

	test("should save in transaction and findall") {
		val count = 10

		// TODO this call is needed to setup the super-node, maybe create super-node in external tx
		postRepository.save(Post(0L, 0L, title = "title", text = "text", author = "author"))

		try {
			withTx { implicit neo =>
				for ( i <- 1 to count-1) postRepository.save(Post(0L, 0L, title = "title", text = "text", author = "author"))
				throw new Exception("aborting transaction")
			}
		} catch {
			case _ =>
		}

		withTx { implicit neo =>
			for ( i <- 1 to count-1) postRepository.save(Post(0L, 0L, title = "title", text = "text", author = "author"))
		}

		assert(postRepository.findAll.toList.size == count)
	}

	test("should find by id") {
		val post = postRepository.save(Post(0L, 0L, title = "title", text = "text", author = "author"))
		val retrieved = postRepository.byId(post.id)
		assert(!retrieved.isEmpty)
		assert(post == retrieved.get)
	}

	test("should update") {
		val post = postRepository.save(Post(0L, 0L, title = "title", text = "text", author = "author"))
		val updated = postRepository.save(post.copy(title = "corrected title"))
	}

	test("should handle tags") {
		val tag = tagRepository.createTag("fooboo")
		val retrieved = tagRepository.byId(tag.id)
		assert(!retrieved.isEmpty)
		assert(tag == retrieved.get)
	}

	test("should handle categories") {
		assert(categoryRepository.findAll.size == Config.postCategories.size)
		val c1 = categoryRepository.save(Category(name = "foo"))

		assert(categoryRepository.findAll.size == Config.postCategories.size+1)
		val c2 = categoryRepository.findByName("foo").get
		assert(c1 == c2)
	}

	test("should save post-category relations") {
		val categories = categoryRepository.findAll()
		val c = categories(0)
		val p1 = Post(title="a foo post", category=c)
		val p2 = postRepository.save(p1)
		val p3 = postRepository.byId(p2.id)
	}

}
