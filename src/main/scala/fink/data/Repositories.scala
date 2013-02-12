package fink.data

import fink.support._

import org.scalatra.ScalatraServlet

import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession

import com.mchange.v2.c3p0.ComboPooledDataSource
import java.util.Properties

sealed trait DataResult

trait Success extends DataResult
object Ok extends Success
case class Created(id: Long) extends Success

trait Failure extends DataResult
object AlreadyExists extends Failure
case class NotFound(message: String) extends Failure

object Repositories {

  def init {
    if (_db.isEmpty) {
      val props = new Properties
      props.load(getClass.getResourceAsStream("/c3p0.properties"))

      val cpds = new ComboPooledDataSource
      cpds.setProperties(props)

      _cpds = Some(cpds)
      _db = Some(Database.forDataSource(cpds))

      try {
        db withSession {
          (Pages.ddl ++ Posts.ddl ++ Tags.ddl ++ Categories.ddl ++ Images.ddl ++ PostTag.ddl ++ Galleries.ddl ++ GalleriesImages.ddl ++ GalleriesTags.ddl).create
        }
      } catch {
        case e:Exception =>
      }
    }
  }

  private var _cpds : Option[ComboPooledDataSource] = None

  private var _db : Option[Database] = None

  def db = _db.getOrElse(throw new Error("Not initialized."))

  def shutdown {
    _cpds map (_.close)
    _cpds = None
    _db = None
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
    val sl = if (!shortlink.isEmpty) shortlink else title
    Pages.withoutId.insert((date, title, author, sl, text))
    DBUtil.insertId
  }

  def update(page: Page) = db withSession {
    byId(page.id).map { page =>
      Pages.where(_.id === page.id).update(page)
      Ok
    } getOrElse NotFound("Could not find page.")
  }

  def delete(pageId: Long) = db withSession {
    if (Pages.where(_.id === pageId).delete > 0) Ok else NotFound("Could not find page.")
  }

}

class PostRepository extends RepositorySupport {

  private def mapPost(post: Post) = {
    post.tags = postTags(post.id).list
    post.category = categoryRepository.byId(post.catId)
    post
  }

  private val postTags = for {
    postId <- Parameters[Long]
    pt <- PostTag if pt.postId === postId
    tag <- Tags if tag.id === pt.tagId
  } yield tag

  def findAll : Seq[Post] = db withSession {
    (for (post <- Posts) yield post).list.map(mapPost)
  }

  def byId(id: Long) : Option[Post] = db withSession {
    Posts.byId(id).firstOption.map(mapPost)
  }

  def byTag(tag: String) : List[Post] = db withSession {
    Posts.byTag(tag).list.map(mapPost)
  }

  def byShortlink(shortlink: String) : Option[Post] = db withSession {
    Posts.byShortlink(shortlink).firstOption.map(mapPost)
  }

  def create(date: Long, title: String, author: String, shortlink: String, text: String, tags: List[String], cat: Option[Category]) : Long = db withSession {
    val sl = if (shortlink != "") shortlink else TemplateHelper.slug(title)

    val catId = cat match {
      case Some(c) if c.id == 0 => categoryRepository.create(c.name) // ...
      case Some(c) => c.id
      case None => 0
    }

    Posts.withoutId.insert((date, catId, title, author, sl, text))
    val postId = DBUtil.insertId

    tags.foreach(tag => addTag(postId, tag))

    postId
  }

  def update(post: Post) = db withSession {
    byId(post.id) map { p =>
      Posts.where(_.id === post.id).update(post)

      p.tags.filterNot(post.tags.contains).foreach(tag => removeTag(post.id, tag.name))
      post.tags.filterNot(p.tags.contains).foreach(tag => addTag(post.id, tag.name))

      Ok
    } getOrElse NotFound("Could not find post.")
  }

  def addTag(postId: Long, tagName: String) : DataResult = db withSession {
    byId(postId) map { post =>
      val tagId = tagRepository.byName(tagName).map(_.id).getOrElse(tagRepository.create(tagName))
      val pt = (for (pt <- PostTag if pt.postId === postId && pt.tagId === tagId) yield pt).firstOption

      if (pt.isEmpty) {
        PostTag.insert(postId, tagId)
        Ok
      } else {
        AlreadyExists
      }
    } getOrElse NotFound("Could not find post.")
  }

  // TODO exists
  def removeTag(postId: Long, tagName: String) : DataResult = db withSession {
    byId(postId) map { post =>
      val tagId = tagRepository.byName(tagName).map(_.id).getOrElse(tagRepository.create(tagName))
      val pt = (for (pt <- PostTag if pt.postId === postId && pt.tagId === tagId) yield pt).firstOption

      if (!pt.isEmpty) {
        PostTag.where(pt => pt.postId === postId && pt.tagId === tagId).delete
      }

      Ok
    } getOrElse NotFound("Could not find post.")
  }

  def modifyCategory(postId: Long, categoryName: String) = db withSession {
    (for {
      post <- byId(postId)
      category <- categoryRepository.byName(categoryName)
    } yield {
      Posts.where(_.id === postId).map(_.catId).update(category.id)
      Ok
    }) getOrElse NotFound("Could not find post or category.")
  }

  def delete(postId: Long) = db withSession {
    if (Posts.where(_.id === postId).delete > 0) Ok else NotFound("Could not find post.")
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
    byId(tag.id) map { tag =>
      Tags.where(_.id === tag.id).update(tag)
      Ok
    } getOrElse NotFound("Could not find tag.")
  }

  def delete(tagId: Long) = db withSession {
    if (Tags.where(_.id === tagId).delete > 0) Ok else NotFound("Could not find tag.")
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
    byId(category.id) map { c =>
      Categories.where(_.id === category.id).update(category)
      Ok
    } getOrElse NotFound("Could not find category.")
  }

  def delete(categoryId: Long) = db withSession {
    if (Categories.where(_.id === categoryId).delete > 0) Ok else NotFound("Could not find category.")
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
    byId(gallery.id) map { g =>
      Galleries.where(_.id === gallery.id).update(gallery)

      g.tags.filterNot(gallery.tags.contains).foreach(tag => removeTag(gallery.id, tag.name))
      gallery.tags.filterNot(g.tags.contains).foreach(tag => addTag(gallery.id, tag.name))

      Ok
    } getOrElse NotFound("Could not find gallery.")
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
    (for {
      gallery <- byId(galleryId)
      image <- imageRepository.byId(imageId)
    } yield {
      val gi = GalleriesImages.where(gi => gi.galleryId === galleryId).map(_.imageId).list
      gi.filter(_ == imageId).headOption match {
        case Some(gi) => AlreadyExists
        case None => GalleriesImages.insert(galleryId, imageId, gi.size.toLong); Ok
      }
    }) getOrElse {
      NotFound("Could not find gallery or image.")
    }
  }

  def removeImage(galleryId: Long, imageId: Long) = db withSession {
    (for {
      gallery <- byId(galleryId)
      image <- imageRepository.byId(imageId)
    } yield {
      val deleted = GalleriesImages.where(gi => gi.galleryId === galleryId && gi.imageId === imageId).delete
      if (gallery.coverId == imageId) {
        Galleries.where(_.id === galleryId).map(_.coverId).update(0)
      }
      if (deleted > 0) Ok else NotFound("Could not find relation between gallery and image.")
    }) getOrElse {
      NotFound("Could not find gallery or image.")
    }
  }

  def addTag(galleryId: Long, tagName: String) : DataResult = db withSession {
    byId(galleryId) map { gallery =>
      val tagId = tagRepository.byName(tagName).map(_.id).getOrElse(tagRepository.create(tagName))
      val gt = (for (gt <- GalleriesTags if gt.galleryId === galleryId && gt.tagId === tagId) yield gt).firstOption

      if (gt.isEmpty) {
        GalleriesTags.insert(galleryId, tagId)
        Ok
      } else {
        AlreadyExists
      }
    } getOrElse NotFound("Could not find gallery.")
  }

  // TODO exists
  def removeTag(galleryId: Long, tagName: String) : DataResult = db withSession {
    byId(galleryId) map { gallery =>
      val tagId = tagRepository.byName(tagName).map(_.id).getOrElse(tagRepository.create(tagName))
      val gt = (for (gt <- GalleriesTags if gt.galleryId === galleryId && gt.tagId === tagId) yield gt).firstOption

      if (!gt.isEmpty) {
        GalleriesTags.where(gt => gt.galleryId === galleryId && gt.tagId === tagId).delete
      }

      Ok
    } getOrElse NotFound("Could not find gallery.")
  }

  def setCover(galleryId: Long, coverId: Long) : DataResult = db withSession {
    (for {
      gallery <- byId(galleryId)
      image <- gallery.images.filter(_.id == coverId).headOption
    } yield {
      Galleries.where(_.id === galleryId).map(_.coverId).update(coverId)
      Ok
    }) getOrElse NotFound("Could not find gallery.")
  }
}

class ImageRepository extends RepositorySupport {
  def findAll : Seq[Image] = db withSession {
    (for (image <- Images) yield image).list
  }

  def byId(id: Long) : Option[Image] = db withSession {
    Images.byId(id).firstOption
  }

  def byHash(hash: String) : Option[Image] = db withSession {
    Images.byHash(hash).firstOption
  }

  def create(date: Long, title: String, author: String, hash: String, contentType: String, filename: String) : Long = db withSession {
    Images.withoutId.insert((date, title, author, hash, contentType, filename))
    DBUtil.insertId
  }

  def update(image: Image) = db withSession {
    val updated = Images.where(_.id === image.id).update(image)
    if (updated > 0) Ok else NotFound("Could not find image.")
  }

  // TODO gallery references: db constraints, error handling
  def delete(id: Long) : DataResult = db withSession {
    val count = Images.where(_.id === id).delete
    if (count > 0) Ok else NotFound("Could not find image.")
  }
}
