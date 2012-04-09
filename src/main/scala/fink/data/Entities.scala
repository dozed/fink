package fink.data

import scala.collection.mutable.HashSet

import org.joda.time.DateTime;
// import java.util.UUID;

case class User(id: Long = 0L, name: String, password: String)

case class Tag(id: Long = 0L, name: String)

case class Category(
	id: Long = 0L,
	name: String = ""
)

trait ContentItem {
	def id: Long
	def date: Long
	def title: String
	def author: String

	// tags business
	var tags = new HashSet[Tag]()

	def clearTags() {
		tags.clear
	}

	def addTag(tag: Tag) {
		tags += tag
	}

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
	text: String = "",
	category: Category = null) {

	var tags = List[Tag]()

}

case class Image(
	id: Long = 0L,
	date: Long = 0L,
	title: String,
	author: String = "",
	full: String,
	medium: String,
	thumb: String
) extends ContentItem

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
