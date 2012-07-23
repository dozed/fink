package fink.data

import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

object Posts extends Table[Post]("posts") {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def catId = column[Long]("catId")

  def date = column[Long]("date")
  def title = column[String]("title")
  def author = column[String]("author")
  def text = column[String]("text")
  def withoutId = date ~ catId ~ title ~ author ~ text
  def * = id ~ catId ~ date ~ title ~ author ~ text <> (Post, Post.unapply _)

  def category = Categories.where(_.id === catId)
  def tags = PostTag.where(_.postId === id)

  val byId = createFinderBy(_.id)
}

object PostTag extends Table[(Long, Long)]("post_tags") {
  def postId = column[Long]("postId")
  def tagId = column[Long]("tagId")
  def * = postId ~ tagId
}

object Tags extends Table[Tag]("tags") {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def withoutId = name
  def * = id ~ name <> (Tag, Tag.unapply _)

  val byName = createFinderBy(_.name)
  val byId = createFinderBy(_.id)
}

object Categories extends Table[Category]("categories") {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def name = column[String]("name")
  def withoutId = name
  def * = id ~ name <> (Category, Category.unapply _)

  val byId = createFinderBy(_.id)
  val byName = createFinderBy(_.name)
}

object Images extends Table[Image]("images") {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def date = column[Long]("date")
  def title = column[String]("title")
  def author = column[String]("author")
  def hash = column[String]("hash")

  def withoutId = date ~ title ~ author ~ hash
  def * = id ~ date ~ title ~ author ~ hash <> (Image, Image.unapply _)

  val byId = createFinderBy(_.id)
}


object Galleries extends Table[Gallery]("galleries") {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def coverId = column[Long]("coverId")
  def date = column[Long]("date")
  def title = column[String]("title")
  def author = column[String]("author")
  def shortlink = column[String]("shortlink")
  def text = column[String]("text")

  def withoutId = coverId ~ date ~ title ~ author ~ shortlink ~ text
  def * = id ~ coverId ~ date ~ title ~ author ~ shortlink ~ text <> (Gallery, Gallery.unapply _)

  val byId = createFinderBy(_.id)
  val byShortlink = createFinderBy(_.shortlink)
}

object GalleriesImages extends Table[(Long, Long)]("galleries_images") {
  def galleryId = column[Long]("galleryId")
  def imageId = column[Long]("imageId")
  def * = galleryId ~ imageId
}

