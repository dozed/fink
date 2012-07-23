package fink.data

import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

case class DataResult

case class Success extends DataResult
case object Ok extends Success
case class Created(id: Long) extends Success

case class Failure extends DataResult
case object AlreadyExists extends Failure
case class NotFound(message: String) extends Failure
case class Error(message: String) extends Failure

object Repositories {
  val db = Database.forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

  db withSession {
    (Posts.ddl ++ Tags.ddl ++ Categories.ddl ++ Images.ddl ++ PostTag.ddl ++ Galleries.ddl ++ GalleriesImages.ddl).create
  }

  val postRepository = new PostRepository
  val tagRepository = new TagRepository
  val categoryRepository = new CategoryRepository
  val imageRepository = new ImageRepository
  val galleryRepository = new GalleryRepository
}

trait RepositorySupport {
  def postRepository = Repositories.postRepository
  def tagRepository = Repositories.tagRepository
  def categoryRepository = Repositories.categoryRepository
  def imageRepository = Repositories.imageRepository
  def galleryRepository = Repositories.galleryRepository

  def db = Repositories.db
}

object DBUtil {
  def insertId = Query(SimpleFunction.nullary[Long]("scope_identity")).first
}

object UserRepository extends RepositorySupport {
  def find(name: String) = {
    Some(User(0, "name", "password"))
  }

  def login(name: String, password: String) = Some(User(0, "name", "password"))
}

class PostRepository extends RepositorySupport {

  def findAll : Seq[Post] = db withSession {
    (for (post <- Posts) yield post).list.map(mapPost)
  }

  def byId(id: Long) : Option[Post] = db withSession {
    Posts.byId(id).firstOption.map(mapPost)
  }

  def mapPost(post: Post) = {
    post.tags = postTags(post.id).list
    post.category = categoryRepository.byId(post.catId)
    post
  }

  val postTags = for {
    postId <- Parameters[Long]
    pt <- PostTag if pt.postId === postId
    tag <- Tags if tag.id === pt.tagId
  } yield tag

  def create(post: Post) : Long = db withSession {
    val catId = post.category match {
      case Some(cat) if cat.id == 0 => categoryRepository.create(cat.name) // ...
      case Some(cat) => cat.id
      case None => 0
    }

    Posts.withoutId.insert((post.date, catId, post.title, post.author, post.text))
    val postId = DBUtil.insertId

    post.tags.foreach(tag => addTag(postId, tag.name))

    postId
  }

  def update(post: Post) = db withSession {
    byId(post.id) match {
      case Some(post) =>
        (for (post <- Posts if post.id === post.id) yield post.title ~ post.author ~ post.text).update(post.title, post.author, post.text)
        Ok
      case None => NotFound("Could not find post: %s".format(post.id))
    }
  }

  def addTag(postId: Long, tagName: String) : DataResult = db withSession {
    byId(postId) match {
      case Some(post) =>
        val tagId = tagRepository.byName(tagName).map(_.id).getOrElse(tagRepository.create(tagName))
        val pt = (for (pt <- PostTag if pt.postId === postId && pt.tagId === tagId) yield pt).firstOption

        if (pt.isEmpty) {
          PostTag.insert(postId, tagId)
          Ok
        } else {
          AlreadyExists
        }
      case None => NotFound("Could not find post: %s".format(postId))
    }
  }

  // TODO exists
  def deleteTag(postId: Long, tagName: String) : DataResult = db withSession {
    byId(postId) match {
      case Some(post) =>
        val tagId = tagRepository.byName(tagName).map(_.id).getOrElse(tagRepository.create(tagName))
        val pt = (for (pt <- PostTag if pt.postId === postId && pt.tagId === tagId) yield pt).firstOption
        
        if (!pt.isEmpty) {
          PostTag.where(pt => pt.postId === postId && pt.tagId === tagId).delete
        }

        Ok
      case None => NotFound("Could not find post: %s".format(postId))
    }
  }

  def modifyCategory(postId: Long, categoryName: String) = db withSession {
    byId(postId) match {
      case Some(post) =>
        categoryRepository.byName(categoryName) match {
          case Some(category) => 
            Posts.where(_.id === postId).map(_.catId).update(category.id)
            Ok
          case None => NotFound("Could not find category: %s".format(categoryName))
        }
      case None => NotFound("Could not find post: %s".format(postId))
    }
  }

  def delete(postId: Long) = db withSession {
    if (Posts.where(_.id === postId).delete > 0) Ok else NotFound("Could not find post: %s".format(postId))
  }

}

class TagRepository extends RepositorySupport {
  def findAll : Seq[Tag] = db withSession {
    (for (tag <- Tags) yield tag).list
  }

  def byId(id: Long) : Option[Tag] = db withSession {
    Tags.byId(id).firstOption
  }

  def byName(name: String) : Option[Tag] = db withSession {
    Tags.byName(name).firstOption
  }

  def create(name: String) : Long = db withSession {
    byName(name)
    Tags.withoutId.insert(name)
    DBUtil.insertId
  }

  def update(tag: Tag) = db withSession {
    byId(tag.id) match {
      case Some(tag) =>
        Tags.where(_.id === tag.id).update(tag)
        Ok
      case None => NotFound("Could not find tag: %s".format(tag.id))
    }
  }

  def delete(tagId: Long) = db withSession {
    if (Tags.where(_.id === tagId).delete > 0) Ok else NotFound("Could not find tag: %s".format(tagId))
  }
}

class CategoryRepository extends RepositorySupport {
  def findAll : Seq[Category] = db withSession {
    (for (category <- Categories) yield category).list
  }

  def create(name: String) : Long = db withSession {
    Categories.withoutId.insert(name)
    DBUtil.insertId
  }

  def byId(id: Long) : Option[Category] = db withSession {
    Categories.byId(id).firstOption
  }

  def byName(name: String) : Option[Category] = db withSession {
    Categories.byName(name).firstOption
  }

  def update(category: Category) = db withSession {
    byId(category.id) match {
      case Some(category) =>
        Categories.where(_.id === category.id).update(category)
        Ok
      case None => NotFound("Could not find category: %s".format(category.id))
    }
  }

  def delete(categoryId: Long) = db withSession {
    if (Categories.where(_.id === categoryId).delete > 0) Ok else NotFound("Could not find category: %s".format(categoryId))
  }
}

class GalleryRepository extends RepositorySupport {
  def findAll : Seq[Gallery] = db withSession {
    (for (gallery <- Galleries) yield gallery).list.map(mapGallery)
  }

  def create(coverId: Long, date: Long, title: String, author: String, shortlink: String, text: String) : Long = db withSession {
    Galleries.withoutId.insert((coverId, date, title, author, shortlink, text))
    DBUtil.insertId
  }

  def mapGallery(gallery: Gallery) = {
    gallery.images = galleriesImages(gallery.id).list
    gallery
  }

  val galleriesImages = for {
    galleryId <- Parameters[Long]
    gi <- GalleriesImages if gi.galleryId === galleryId
    image <- Images if image.id === gi.imageId
  } yield image

  def update(gallery: Gallery) = db withSession {
    val updated = Galleries.where(_.id === gallery.id).update(gallery)
    if (updated > 0) Ok else NotFound("Could not find gallery: %s".format(gallery.id))
  }

  def byId(id: Long) : Option[Gallery] = db withSession {
    Galleries.byId(id).firstOption.map(mapGallery)
  }

  def delete(id: Long) : DataResult = db withSession {
    val count = Galleries.where(_.id === id).delete
    if (count > 0) Ok else NotFound("Could not find gallery: %s".format(id))
  }

  def addImage(galleryId: Long, imageId: Long) = db withSession {
    byId(galleryId) match {
      case Some(gallery) =>
        imageRepository.byId(imageId) match {
          case Some(image) =>
            GalleriesImages.where(gi => gi.galleryId === galleryId && gi.imageId === imageId).firstOption match {
              case Some(gi) =>
                AlreadyExists
              case None =>
                GalleriesImages.insert(galleryId, imageId)
                Ok
            }
          case None => NotFound("Could not find image: %s".format(imageId))
        }
      case None => NotFound("Could not find gallery: %s".format(galleryId))
    }
  }

  def removeImage(galleryId: Long, imageId: Long) = db withSession {
    byId(galleryId) match {
      case Some(gallery) =>
        imageRepository.byId(imageId) match {
          case Some(image) =>
            val deleted = GalleriesImages.where(gi => gi.galleryId === galleryId && gi.imageId === imageId).delete
            if (deleted > 0) Ok else NotFound("Could not find relation between gallery and image.")
          case None => NotFound("Could not find image: %s".format(imageId))
        }
      case None => NotFound("Could not find gallery: %s".format(galleryId))
    }
  }
}

class ImageRepository extends RepositorySupport {
  def findAll : Seq[Image] = db withSession {
    (for (image <- Images) yield image).list
  }

  def byId(id: Long) : Option[Image] = db withSession {
    Images.byId(id).firstOption
  }

  def create(date: Long, title: String, author: String, hash: String) : Long = db withSession {
    Images.withoutId.insert((date, title, author, hash))
    DBUtil.insertId
  }

  def update(image: Image) = db withSession {
    val updated = Images.where(_.id === image.id).update(image)
    if (updated > 0) Ok else NotFound("Could not find image: %s".format(image.id))
  }

  // TODO gallery references: db constraints, error handling
  def delete(id: Long) : DataResult = db withSession {
    val count = Images.where(_.id === id).delete
    if (count > 0) Ok else NotFound("Could not find image: %s".format(id))
  }
}
