package fink

import scala.collection.JavaConversions._

import fink.data._

import org.specs2.mutable._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.Serialization.{read => jsread, write => jswrite}

class JsonTests extends Specification {

	implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[Page], classOf[Category], classOf[Post], classOf[Tag]))) + FieldSerializer[Post]()

	"should de/serialize posts" in {
		val post = Post(1,2,3,"title","author","text")
		post.tags = List(Tag(0, "foo"), Tag(1, "bar"))
		post.category = Some(Category(0, "cat"))

		val json = jswrite[Post](post)
		val post2 = jsread[Post](json)

		post2 must beEqualTo(post)
		post2.tags must containAllOf(post.tags)
		post2.category must beEqualTo(post.category)
	}

	"should handle missing options" in {
		val json = """{"tags":[],"id":1,"catId":0,"date":0,"title":"title","author":"author","text":"text"}"""
		val post = jsread[Post](json)
		post.category must beNone
		post.tags must have size(0) 
	}

}
