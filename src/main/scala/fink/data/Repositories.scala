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
}

trait RepositorySupport {
	def imageRepository = Repositories.imageRepository
	def tagRepository = Repositories.tagRepository
	def postRepository = Repositories.postRepository
	def pageRepository = Repositories.pageRepository
	def mediaRepository = Repositories.mediaRepository
	def categoryRepository = Repositories.categoryRepository
}

trait Repository[T <: AnyRef] {
	def save(t: T)(implicit m: Manifest[T]) : T
	def update(t: T)(implicit m: Manifest[T]) : Option[T]
	def byId(id: Long)(implicit m: Manifest[T]) : Option[T]
	def delete(id: Long)
	def findAll()(implicit m: Manifest[T]) : List[T]
	def superNode(implicit m: Manifest[T]) : Node
	def node(t: T) : Option[Node]

	// the following methods need to be implemented in the user repository
	// TODO use type classes: Locator

	/**
	 * Maps the identity from the Node back into the user object or stores it in a cache.
	 *
	 * @param t The user object.
	 * @param node The retrieved node.
	 * @returns A copy of the user object, in most cases with an attached identifier.
	 */
	def handleIdentity(t: T, node: Node) : T

	/**
	 * Extracts the identity from a user object.
	 * TODO use @Key
	 *
	 * @param t The user object.
	 * @return The object's identity.
	 */
	def getIdentity(t: T) : Long
}

trait ContentItemRepository[T <: AnyRef] extends Repository[T] with GraphDatabaseServiceProvider with Neo4jWrapper with Neo4jIndexProvider with TypedTraverser{

	val ds = ContentItemRepository.ds

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
					val n = createNode(t)(ds)
					superNode --> "sub" --> n
					handleIdentity(t, n)	
				}
			case _ => // already saved, update
				update(t).get
		}
	}

	def update(t: T)(implicit m: Manifest[T]) : Option[T] = {
		withTx { implicit neo =>
			for {
				id <- Option(getIdentity(t))
				node <- Option(ds.gds.getNodeById(id))
			} yield {
				serialize(t, node)
				t
			}
			// Option(ds.gds.getNodeById(getIdentity(t))).map(Neo4jWrapper.serialize(t, _))
		}
	}

	def byId(id: Long)(implicit m: Manifest[T]) : Option[T] = {
		try {
			val item = Option(ds.gds.getNodeById(id))
				.map(n => (n, Neo4jWrapper.toCC[T](n)))
				.map(a => handleIdentity(a._2.get, a._1))
			item
		} catch {
			case e: NotFoundException => None
		}
	}

	def node(t: T) : Option[Node] = getIdentity(t) match {
		case 0L => None
		case id:Long => Option(ds.gds.getNodeById(id))
	}

	def delete(id: Long) {
		withTx { implicit neo =>
			val node = ds.gds.getNodeById(id)
			node.getRelationships().foreach(_.delete())
			node.delete()
		}
	}

	def handleIdentity(t: T, node: Node) : T
	def getIdentity(t: T) : Long

	// import ContentItemRepository._

	// def coolSave(t: T)(implicit m: Manifest[T]) : ContentItem2Node[T] = {
	// 	withTx { implicit neo =>
	// 		val n = createNode(t)(ds)
	// 		getReferenceNode --> classTag --> n
	// 		ContentItem2Node[T](t, n)
	// 	}
	// }

	private def classTag(implicit m: Manifest[T]) = "__class_%s__".format(m.toString)

	def findAll()(implicit m: Manifest[T]) : List[T] = {
		superNode.getRelationships(Direction.OUTGOING)
			.map(_.getEndNode).toList
			.map(n => (n, Neo4jWrapper.toCC[T](n)))
			.map(a => handleIdentity(a._2.get, a._1))
	}
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
		println("shutting down gds")
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
	def handleIdentity(item: Post, node: Node) = item.copy(id = node.getId)
	def getIdentity(item: Post) = item.id

	def createPost(title: String, text: String, author: String, category: String, tags: String) = {
		val post = Post(0L, 0L, title = title, text = text, author = author)
		save(post)
	}

	def findPostByUuid(uuid: String) : Option[Post] = None

	def findPost(year: Int, month: Int, day: Int, title: String) : Option[Post] = None

	override def byId(id: Long)(implicit m: Manifest[Post]) : Option[Post] = {
		try {
			for {
				node <- Option(ds.gds.getNodeById(id))
				post <- Neo4jWrapper.toCC[Post](node)
				postWithId <- Option(handleIdentity(post, node))
			} yield {
				val tags = node.getRelationships(Direction.OUTGOING, "tags").toList.map {
					rel =>
						val tn = rel.getEndNode
						val tag = Neo4jWrapper.toCC[Tag](tn).get
						tagRepository.handleIdentity(tag, tn)
				}

				var post = postWithId

				node.getRelationships(Direction.OUTGOING, "category").toList.map {
					rel =>
						val tn = rel.getEndNode
						val tag = Neo4jWrapper.toCC[Category](tn).get
						categoryRepository.handleIdentity(tag, tn)
				}.headOption match {
					case Some(category) => post.category = Some(category)
					case None => 
				}

				post.tags = tags
				post
			}
		} catch {
			case e: NotFoundException => None
		}
	}

	override def save(t: Post)(implicit m: Manifest[Post]) : Post = {
		withTx { implicit neo =>
			val n = createNode(t)(ds)

			// index node
			superNode --> "sub" --> n

			val np = handleIdentity(t, n)

			// handle relationships
			t.category.map { c =>
				val cat = categoryRepository.save(c)
				val catNode = categoryRepository.node(c)
				n --> "category" --> catNode.get
				np.category = Some(cat)
			}

			np.tags = t.tags.map { tag =>
				val t = tagRepository.save(tag)
				val tn = tagRepository.node(t).get
				n --> "tags" --> tn
				t
			}

			np
		}
	}

	override def update(t: Post)(implicit m: Manifest[Post]) : Option[Post] = {
		withTx { implicit neo =>
			for {
				id <- Option(getIdentity(t))
				node <- Option(ds.gds.getNodeById(id))
			} yield {
				serialize(t, node)
				
				node.getRelationships(Direction.OUTGOING, "category").foreach { r =>
					r.delete()
				}

				t.category.map { c =>
					println("updating cat: %s".format(c))
					val cat = categoryRepository.save(c)
					val catNode = categoryRepository.node(c).get
					node --> "category" --> catNode
					t.category = Some(cat)
				}

				println("tags: " + node.getRelationships(Direction.OUTGOING, "tags").toList.size())

				node.getRelationships(Direction.OUTGOING, "tags").foreach { r =>
					r.delete()
				}

				t.tags = t.tags.map { tag =>
					val t = tagRepository.save(tag)
					val tn = tagRepository.node(t).get
					node --> "tags" --> tn
					t
				}

				t
			}
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

