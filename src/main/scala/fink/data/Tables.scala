package fink.data

import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession

object Pages extends Table[Page]("pages") {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def date = column[Long]("date")
  def title = column[String]("title")
  def author = column[String]("author")
  def shortlink = column[String]("shortlink")
  def text = column[String]("text")
  def withoutId = date ~ title ~ author ~ shortlink ~ text
  def * = id ~ date ~ title ~ author ~ shortlink ~ text <> (Page, Page.unapply _)

  val byId = createFinderBy(_.id)
  val byShortlink = createFinderBy(_.shortlink)
}

object Posts extends Table[Post]("posts") {
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  def catId = column[Long]("catId")

  def date = column[Long]("date")
  def title = column[String]("title")
  def author = column[String]("author")
  def text = column[String]("text")
  def shortlink = column[String]("shortlink")
  def withoutId = date ~ catId ~ title ~ author ~ shortlink ~ text
  def * = id ~ catId ~ date ~ title ~ author ~ shortlink ~ text <> (Post, Post.unapply _)

  def category = Categories.where(_.id === catId)
  def tags = PostTag.where(_.postId === id)

  val byId = createFinderBy(_.id)
  val byShortlink = createFinderBy(_.shortlink)

  val byTag = for {
    tagName <- Parameters[String]
    t <- Tags if (t.name === tagName)
    pt <- PostTag if (pt.tagId === t.id)
    p <- Posts if (pt.postId === p.id)
  } yield p

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
  def contentType = column[String]("contentType")
  def filename = column[String]("filename")

  def withoutId = date ~ title ~ author ~ hash ~ contentType ~ filename
  def * = id ~ date ~ title ~ author ~ hash ~ contentType ~ filename <> (Image, Image.unapply _)

  val byId = createFinderBy(_.id)
  val byHash = createFinderBy(_.hash)
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

object GalleriesImages extends Table[(Long, Long, Long)]("galleries_images") {
  def galleryId = column[Long]("galleryId")
  def imageId = column[Long]("imageId")
  def sort = column[Long]("sort")
  def * = galleryId ~ imageId ~ sort
}

object GalleriesTags extends Table[(Long, Long)]("galleries_tags") {
  def galleryId = column[Long]("galleryId")
  def tagId = column[Long]("tagId")
  def * = galleryId ~ tagId
}
