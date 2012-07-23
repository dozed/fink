package fink

import org.specs2.mutable._

import fink.data._

import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql.extended.H2Driver.Implicit._
import org.scalaquery.ql.extended.{ExtendedTable => Table}

class DataTest extends Specification with RepositorySupport {

  sequential

  "should create tag" in {
    tagRepository.create("foo")
    tagRepository.create("bar")
    tagRepository.create("baz")

    tagRepository.findAll must have size(3)
    tagRepository.byId(1) must beSome.which(_.name.equals("foo"))
    tagRepository.byId(2) must beSome.which(_.id == 2)
    tagRepository.byId(4) must beNone
  }

  "should create categories" in {
    categoryRepository.create("foo")
    categoryRepository.create("bar")
    categoryRepository.create("baz")

    categoryRepository.findAll must have size(3)
    categoryRepository.byId(1) must beSome.which(_.name.equals("foo"))
    categoryRepository.byId(2) must beSome.which(_.id == 2)
    categoryRepository.byId(4) must beNone
  }

  "should create post" in {
    def dummyPost = {
      val p = Post(0, 0, 0, "title", "author", "text")
      p.category = Some(Category(0, "foo"))
      p.tags = List(Tag(0, "x"), Tag(0, "y"), tagRepository.findAll(2))
      p
    }

    val a = dummyPost
    val id = postRepository.create(a)
    val p1 = postRepository.byId(id)

    p1 must beSome.which(p => p.tags.map(_.name) must containAllOf(a.tags.map(_.name)))

    val p2 = postRepository.findAll(0)

    p1 must beSome.which(p => p.id == 1 && (p.category must beSome) && (p.tags must have size(3)))
    p1 must beSome.which(p => p == p2 && p.category == p2.category && (p.tags -- p2.tags).size == 0)
    tagRepository.findAll must have size(5)

    postRepository.create(Post(0, 0, 0, "title", "author", "text"))
    postRepository.create(Post(0, 0, 0, "title", "author", "text"))

    postRepository.byId(2) must beSome.which(_.id == 2)
    postRepository.byId(4) must beNone
  }


  "should create images" in {
    imageRepository.create(0, "foo", "author", "hash")
    imageRepository.create(0, "foo", "author", "hash")

    imageRepository.findAll must have size(2)
    imageRepository.byId(1) must beSome.which(_.hash.equals("hash"))
    imageRepository.byId(2) must beSome.which(_.id == 2)
    imageRepository.byId(4) must beNone
  }

}