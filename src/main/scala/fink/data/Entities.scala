package fink.data

import scala.collection.mutable.HashSet

import org.joda.time.DateTime;

case class User(id: Long, name: String, password: String)

case class Tag(id: Long, name: String)

case class Category(id: Long, name: String)

case class Post(
  id: Long,
  catId: Long,
  date: Long,
  title: String,
  author: String,
  text: String) {

  var tags = List[Tag]()
  var category : Option[Category] = None
}

case class Page(
  id: Long,
  date: Long,
  title: String,
  author: String,
  shortlink: String,
  text: String
)

case class Image(
  id: Long,
  date: Long,
  title: String,
  author: String,
  hash: String
)

case class Gallery(
  id: Long,
  coverId: Long,
  date: Long,
  title: String,
  author: String,
  shortlink: String,
  text: String
) {

  var images = List[Image]()
  var tags = List[Tag]()
  var cover: Option[Image] = None
}
