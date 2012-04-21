package fink.data

import scala.collection.mutable.HashSet

import org.joda.time.DateTime;
// import java.util.UUID;

case class User(id: Long = 0L, name: String, password: String)

case class Tag(id: Long = 0L, name: String)

case class Category(id: Long = 0L, name: String = "")

trait ContentItem {
	def id: Long
	def date: Long
	def title: String
	def author: String

	// tags business
	var tags = new HashSet[Tag]()

}

case class Page(
	id: Long = 0L,
	date: Long = 0L,
	title: String = "",
	author: String = "",
	shortlink: String = "",
	text: String = ""
) extends ContentItem

case class Post(
	id: Long = 0L,
	date: Long = 0L,
	title: String,
	author: String = "",
	text: String = "") {

	var tags = List[Tag]()
	// def tags = _tags
	// def tags_=(tags:List[Tag]) { _tags = tags }

	var category : Option[Category] = None
	// def category = _category
	// def category_=(c:Category) { _category = Some(c) }

	// fetch relations from other instance
	def copyRelations(post:Post) = {
		tags = post.tags
		category = post.category
		this
	}
}

case class Image(
	id: Long = 0L,
	date: Long = 0L,
	title: String = "",
	author: String = "",
	full: String,
	medium: String,
	thumb: String
)

case class Gallery(
	id: Long = 0L,
	date: Long = 0L,
	title: String = "",
	shortlink: String = "",
	text: String = ""
) {
	var images = List[Image]()
	var tags = List[Tag]()
	var cover: Option[Image] = None

	def copyRelations(gallery:Gallery) = {
		tags = gallery.tags
		images = gallery.images
		this
	}
}

case class MediaCollection(
	id: Long = 0L,
	date: Long = 0L,
	title: String,
	author: String = "",
	shortlink: String = "",
	cover: Image
) extends ContentItem {

	var items = new HashSet[Image]()

	def setCover(image: Image) {

	}

	def removeItem(image: Image) {
		items -= image
	}

	def addItem(image: Image) {
		items += image
	}
}
