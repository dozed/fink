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
    (Pages.ddl ++ Posts.ddl ++ Tags.ddl ++ Categories.ddl ++ Images.ddl ++ PostTag.ddl ++ Galleries.ddl ++ GalleriesImages.ddl ++ GalleriesTags.ddl).create
  }

  val pageRepository = new PageRepository
  val postRepository = new PostRepository
  val tagRepository = new TagRepository
  val categoryRepository = new CategoryRepository
  val imageRepository = new ImageRepository
  val galleryRepository = new GalleryRepository
}

trait RepositorySupport {
  def pageRepository = Repositories.pageRepository
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

class PageRepository extends RepositorySupport {

  def findAll : Seq[Page] = db withSession {
    (for (page <- Pages) yield page).list
  }

  def byId(id: Long) : Option[Page] = db withSession {
    Pages.byId(id).firstOption
  }

  def byShortlink(shortlink: String) : Option[Page] = db withSession {
    Pages.byShortlink(shortlink).firstOption
  }

  def create(date: Long, title: String, author: String, shortlink: String, text: String) : Long = db withSession {
    Pages.withoutId.insert((date, title, author, shortlink, text))
    DBUtil.insertId
  }

  def update(page: Page) = db withSession {
    byId(page.id) match {
      case Some(p) =>
        Pages.where(_.id === page.id).update(page)
        Ok
      case None => NotFound("Could not find page: %s".format(page.id))
    }
  }

  def delete(pageId: Long) = db withSession {
    if (Pages.where(_.id === pageId).delete > 0) Ok else NotFound("Could not find page: %s".format(pageId))
  }

}

class PostRepository extends RepositorySupport {

  def findAll : Seq[Post] = db withSession {
    (for (post <- Posts) yield post).list.map(mapPost)
  }

  def byId(id: Long) : Option[Post] = db withSession {
    Posts.byId(id).firstOption.map(mapPost)
  }

  def byTitle(title: String) : Option[Post] = db withSession {
    Posts.byTitle(title).firstOption.map(mapPost)
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
      case Some(p) =>
        Posts.where(_.id === post.id).update(post)

        (p.tags -- post.tags).foreach(tag => removeTag(post.id, tag.name))
        (post.tags -- p.tags).foreach(tag => addTag(post.id, tag.name))

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
  def removeTag(postId: Long, tagName: String) : DataResult = db withSession {
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
      case Some(t) =>
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
      case Some(c) =>
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

  def create(coverId: Long, date: Long, title: String, author: String, shortlink: String, text: String, tags: List[String]) : Long = db withSession {
    Galleries.withoutId.insert((coverId, date, title, author, shortlink, text))
    val galleryId = DBUtil.insertId
    tags.foreach(tag => addTag(galleryId, tag))
    galleryId
  }

  def mapGallery(gallery: Gallery) = db withSession {
    gallery.images = galleriesImages(gallery.id).list
    gallery.tags = galleriesTags(gallery.id).list
    gallery
  }

  val galleriesImages = for {
    galleryId <- Parameters[Long]
    gi <- GalleriesImages if gi.galleryId === galleryId
    _ <- Query orderBy gi.sort
    image <- Images if image.id === gi.imageId
  } yield image

  val galleriesTags = for {
    galleryId <- Parameters[Long]
    gi <- GalleriesTags if gi.galleryId === galleryId
    tag <- Tags if tag.id === gi.tagId
  } yield tag

  def update(gallery: Gallery) = db withSession {
    byId(gallery.id) match {
      case Some(g) =>
        Galleries.where(_.id === gallery.id).update(gallery)

        (g.tags -- gallery.tags).foreach(tag => removeTag(gallery.id, tag.name))
        (gallery.tags -- g.tags).foreach(tag => addTag(gallery.id, tag.name))

        Ok
      case None =>
        NotFound("Could not find gallery: %s".format(gallery.id))
    }
  }

  def updateImageOrder(id: Long, imageIds: List[Long]) = db withSession {
    imageIds.zipWithIndex.foreach { case (imageId, index) =>
      GalleriesImages.where(gi => gi.galleryId === id && gi.imageId === imageId).map(_.sort).update(index)
    }
    Ok
  }

  def byId(id: Long) : Option[Gallery] = db withSession {
    Galleries.byId(id).firstOption.map(mapGallery)
  }

  def byShortlink(shortlink: String) : Option[Gallery] = db withSession {
    Galleries.byShortlink(shortlink).firstOption.map(mapGallery)
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
            val gi = GalleriesImages.where(gi => gi.galleryId === galleryId).map(_.imageId).list
            gi.filter(_ == imageId).headOption match {
              case Some(gi) =>
                AlreadyExists
              case None =>
                GalleriesImages.insert(galleryId, imageId, gi.size.toLong)
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

  def addTag(galleryId: Long, tagName: String) : DataResult = db withSession {
    byId(galleryId) match {
      case Some(gallery) =>
        val tagId = tagRepository.byName(tagName).map(_.id).getOrElse(tagRepository.create(tagName))
        val gt = (for (gt <- GalleriesTags if gt.galleryId === galleryId && gt.tagId === tagId) yield gt).firstOption

        if (gt.isEmpty) {
          GalleriesTags.insert(galleryId, tagId)
          Ok
        } else {
          AlreadyExists
        }
      case None => NotFound("Could not find gallery: %s".format(galleryId))
    }
  }

  // TODO exists
  def removeTag(galleryId: Long, tagName: String) : DataResult = db withSession {
    byId(galleryId) match {
      case Some(gallery) =>
        val tagId = tagRepository.byName(tagName).map(_.id).getOrElse(tagRepository.create(tagName))
        val gt = (for (gt <- GalleriesTags if gt.galleryId === galleryId && gt.tagId === tagId) yield gt).firstOption
        
        if (!gt.isEmpty) {
          GalleriesTags.where(gt => gt.galleryId === galleryId && gt.tagId === tagId).delete
        }

        Ok
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
