package fink

import scala.collection.JavaConversions._

import fink.data._

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import org.scalatest.BeforeAndAfterAll

import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}

class JsonTests extends FunSuite {

	implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[Page], classOf[Category], classOf[Post], classOf[Tag]))) + FieldSerializer[Post]()

	test("case class relationships") {
		val a = """{"abs":"asdkjhsad","tags":[{"id":40,"name":"asd"}],"category":{"id":3,"name":"article"},"id":43,"date":0,"title":"a foo post","author":"","text":""}""";
		val b = read[Post](a)
		println(b)
		println(b.tags)
		println(b.category)
	}

}
