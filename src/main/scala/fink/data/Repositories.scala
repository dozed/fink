package fink.data

import fink.support.Config

import scala.collection.mutable.Set
import scala.sys.ShutdownHookThread
import scala.util.control.Breaks._
import scala.collection.JavaConversions._

import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Relationship
import org.neo4j.graphdb.PropertyContainer
import org.neo4j.graphdb.NotFoundException

import org.neo4j.scala.GraphDatabaseServiceProvider
import org.neo4j.scala.EmbeddedGraphDatabaseServiceProvider
import org.neo4j.scala.Neo4jIndexProvider
import org.neo4j.scala.Neo4jWrapper
import org.neo4j.scala.Neo4jWrapperImplicits
import org.neo4j.scala.TypedTraverser

import org.joda.time.DateTime

// 
// trait Locator[T]
// trait ConcreteRepository extends Locator[Post], Locator[Page]
//

object Repositories {
	val imageRepository = new ImageRepository
	val tagRepository = new TagRepository
	val postRepository = new PostRepository
	val pageRepository = new PageRepository
	val mediaRepository = new MediaRepository
	val categoryRepository = new CategoryRepository
	val galleryRepository = new GalleryRepository
}

trait RepositorySupport {
	def imageRepository = Repositories.imageRepository
	def tagRepository = Repositories.tagRepository
	def postRepository = Repositories.postRepository
	def pageRepository = Repositories.pageRepository
	def mediaRepository = Repositories.mediaRepository
	def categoryRepository = Repositories.categoryRepository
	def galleryRepository = Repositories.galleryRepository
}

trait Repository[T <: AnyRef] {
	def save(t: T)(implicit m: Manifest[T]) : T
	def update(t: T)(implicit m: Manifest[T]) : T
	def byId(id: Long)(implicit m: Manifest[T]) : Option[T]
	def findAll()(implicit m: Manifest[T]) : List[T]
	def delete(id: Long)
	def node(t: T) : Option[Node]
}

trait ContentItemRepository[T <: AnyRef] extends Repository[T] with GraphDatabaseServiceProvider with Neo4jWrapper with Neo4jIndexProvider {

	val ds = ContentItemRepository.ds

	private def classTag(implicit m: Manifest[T]) = "__class_%s__".format(m.toString)

	private var _superNode : Option[Node] = None

	def superNode(implicit m: Manifest[T]) = {
		if (_superNode.isEmpty) {
			ds.gds.getReferenceNode.getSingleRelationship(classTag, Direction.OUTGOING) match {
				case rel:Relationship =>
					_superNode = Some(rel.getEndNode)
				case null =>
					withTx { implicit neo =>
						val n = createNode(ds)
						ds.gds.getReferenceNode --> classTag --> n
						_superNode = Some(n)
					}
			}
		}
		_superNode.get
	}

	// creates a new node or updates an existing
	def save(t: T)(implicit m: Manifest[T]) : T = {
		getIdentity(t) match {
			case 0L =>
				withTx { implicit neo =>
					val node = createNode(t)(ds)
					superNode --> "sub" --> node
					val tn = handleIdentity(t, node)	
					persistRelationships(tn, node)
					tn
				}
			case _ => // already saved, update
				update(t)
		}
	}

	def update(t: T)(implicit m: Manifest[T]) : T = {
		withTx { implicit neo =>
			for {
				id <- Option(getIdentity(t))
				node <- Option(ds.gds.getNodeById(id))
			} yield {
				serialize(t, node)
				persistRelationships(t, node)
			}

			t
		}
	}

	def byId(id: Long)(implicit m: Manifest[T]) : Option[T] = {
		try {
			for {
				node <- Option(ds.gds.getNodeById(id))
				post <- Neo4jWrapper.toCC[T](node)
				postWithId <- Option(handleIdentity(post, node))
			} yield {
				loadRelationships(postWithId, node)
				postWithId
			}
		} catch {
			case e: NotFoundException => None
		}
	}

	def findAll()(implicit m: Manifest[T]) : List[T] = {
		superNode.getRelationships(Direction.OUTGOING).map(r => byId(r.getEndNode.getId)).toList.flatten
	}

	def node(t: T) : Option[Node] = getIdentity(t) match {
		case 0L => None
		case id:Long => Option(ds.gds.getNodeById(id))
	}

	def delete(id: Long) {
		withTx { implicit neo =>
			val node = ds.gds.getNodeById(id)
			node.getRelationships().foreach(_.delete())
			deleteRelationships(node)
			node.delete()
		}
	}

	protected def handleIdentity(t: T, node: Node) : T
	protected def getIdentity(t: T) : Long
	protected def persistRelationships(t: T, node: Node) : Unit = ()
	protected def loadRelationships(t: T, node: Node) : Unit = ()
	protected def deleteRelationships(node: Node) : Unit = ()

}
 
object ContentItemRepository extends EmbeddedGraphDatabaseServiceProvider with Neo4jWrapper with Neo4jIndexProvider with TypedTraverser {
	def neo4jStoreDir = Config.databaseDirectory

	def clear() {
		withTx { implicit neo =>
			ds.gds.getAllNodes().foreach { n =>
				if (n.getId != 0L) {
					n.getRelationships(Direction.BOTH).foreach(_.delete())
					n.delete()
				}
			}
		}
	}

  def querySingle[T <: PropertyContainer](index: Index[T], key: String, query: Any): Option[T] = {
		val hits = index.query(key, query)
		if (hits.hasNext) Option(hits.next) else None
	}

	implicit def contentItem2Node[T](ci: ContentItem2Node[T]) : T = ci.item
	implicit def node2ContentItem[T](ci: ContentItem2Node[T]) : Node = ci.node

	def shutdown() {
		shutdown(ds)
	}

	ShutdownHookThread {
		shutdown()
	}
}

case class ContentItem2Node[T](item: T, node: Node)

class CategoryRepository extends ContentItemRepository[Category] {
	def handleIdentity(item: Category, node: Node) = item.copy(id = node.getId)
	def getIdentity(item: Category) = item.id
	def findByName(name: String) : Option[Category] = findAll().filter(_.name.equals(name)).headOption
	def createIfNotExist(categories: List[String]) = categories.map(name => findByName(name).getOrElse(save(Category(name=name))))
}

class ImageRepository extends ContentItemRepository[Image] {
	def handleIdentity(item: Image, node: Node) = item.copy(id = node.getId)
	def getIdentity(item: Image) = item.id

	def createImage(title: String, full: String, medium: String, thumb: String) = {
		val image = Image(title = title, full = full, medium = medium, thumb = thumb)
		save(image)
	}
}

class TagRepository extends ContentItemRepository[Tag] {
	def handleIdentity(item: Tag, node: Node) = item.copy(id = node.getId)
	def getIdentity(item: Tag) = item.id

	def findTag(name: String) : Option[Tag] = {
		Some(Tag(name = "foo"))
	}

	def createTag(name: String) : Tag = {
		val tag = Tag(name = name)
		save(tag)
	}
}

class PostRepository extends ContentItemRepository[Post] with RepositorySupport {

	def findPost(year: Int, month: Int, day: Int, title: String) : Option[Post] = None

	override def handleIdentity(item: Post, node: Node) = item.copy(id = node.getId).copyRelations(item)
	override def getIdentity(item: Post) = item.id

	override def loadRelationships(post: Post, node: Node) : Unit = {
		post.tags = node.getRelationships(Direction.OUTGOING, "tags").toList.map { rel =>
			val tn = rel.getEndNode
			val tag = Neo4jWrapper.toCC[Tag](tn).get
			tagRepository.handleIdentity(tag, tn)
		}

		node.getRelationships(Direction.OUTGOING, "category").toList.map { rel =>
			val tn = rel.getEndNode
			val tag = Neo4jWrapper.toCC[Category](tn).get
			categoryRepository.handleIdentity(tag, tn)
		}.headOption match {
			case Some(category) => post.category = Some(category)
			case None => 
		}
	}

	override def persistRelationships(post: Post, node: Node) : Unit = {
		node.getRelationships(Direction.OUTGOING, "category").foreach { r =>
			r.delete()
		}

		node.getRelationships(Direction.OUTGOING, "tags").foreach { r =>
			r.delete()
		}

		// can be null due to lift-json mapping null to Some(null)
		post.category = post.category.flatMap(Option(_))

		// handle 1:1 relationships
		post.category match {
			case Some(c) =>
				val category = categoryRepository.save(c)
				val categoryNode = categoryRepository.node(category)
				node --> "category" --> categoryNode.get
				post.category = Some(category)

			case _ =>
				val categories = categoryRepository.findAll()
				val category = categories(0)
				val categoryNode = categoryRepository.node(category)
				node --> "category" --> categoryNode.get
				post.category = Some(category)
		}

		// handle 1:n relationships
		post.tags = post.tags.map { t =>
			val tag = tagRepository.save(t)
			val tagNode = tagRepository.node(tag).get
			node --> "tags" --> tagNode
			tag
		}
	}
}

class GalleryRepository extends ContentItemRepository[Gallery] with RepositorySupport {
	def handleIdentity(item: Gallery, node: Node) = item.copy(id = node.getId).copyRelations(item)
	def getIdentity(item: Gallery) = item.id

	override def loadRelationships(gallery: Gallery, node: Node) : Unit = {
		gallery.images = node.getRelationships(Direction.OUTGOING, "images").toList.map { rel =>
			Neo4jWrapper.toCC[Image](rel.getEndNode).map(imageRepository.handleIdentity(_, rel.getEndNode))
		}.flatten

		gallery.tags = node.getRelationships(Direction.OUTGOING, "tags").toList.map { rel =>
			Neo4jWrapper.toCC[Tag](rel.getEndNode).map(tagRepository.handleIdentity(_, rel.getEndNode))
		}.flatten
	}

	override def persistRelationships(gallery: Gallery, node: Node) : Unit = {
		node.getRelationships(Direction.OUTGOING, "images").foreach { r =>
			r.delete()
		}

		node.getRelationships(Direction.OUTGOING, "tags").foreach { r =>
			r.delete()
		}

		gallery.tags = gallery.tags.map { t =>
			val tag = tagRepository.save(t)
			val tagNode = tagRepository.node(tag).get
			node --> "tags" --> tagNode
			tag
		}

		gallery.images = gallery.images.map { i =>
			val image = imageRepository.save(i)
			val imageNode = imageRepository.node(i).get
			node --> "images" --> imageNode
			image
		}
	}
}

class PageRepository extends ContentItemRepository[Page] {
	def handleIdentity(item: Page, node: Node) = item.copy(id = node.getId)
	def getIdentity(item: Page) = item.id

	def createPage(title: String, shortlink: String, author: String, parent: Option[Page]) : Page = {
		val page = Page(id = 0L, date = 0L, title = title, author = author, shortlink = shortlink, text = "")
		save(page)
	}

	def find(key: String, value: String) : Option[Page] = None

	def findPageByUuid(uuid: String) : Option[Page] = None

	def updatePage(uuid: String, parent: String, title: String, shortlink: String, author: String, text: String, tags: List[String]) {

	}

}

class MediaRepository extends ContentItemRepository[MediaCollection] {
	def handleIdentity(item: MediaCollection, node: Node) = item.copy(id = node.getId)
	def getIdentity(item: MediaCollection) = item.id

	def createCollection(title: String) : MediaCollection = {
		val collection = MediaCollection(id = 0L, date = 0L, title = title, author = "", shortlink = "", cover= null)
		collection
	}

	def findCollection(id: String) : Option[MediaCollection] = None

	def findCollections() : List[MediaCollection] = List[MediaCollection]()

	def unlinkImage(collection: MediaCollection, image: Image) {

	}

	def deleteImage(collection: MediaCollection, image: Image) {

	}

	def saveImage(image: Image) {

	}

	def sortImages(collection: MediaCollection, order: List[String]) {

	}

}

object UserRepository extends ContentItemRepository[User] {
	def handleIdentity(item: User, node: Node) = item.copy(id = node.getId)
	def getIdentity(item: User) = item.id

	def find() : Option[User] = Some(User(0L, "admin", "admin"))
	def find(username: String) : Option[User] = Some(User(0L, "admin", "admin"))
	def login(username: String, password: String) = find()
}

