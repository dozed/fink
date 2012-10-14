package fink.data

import org.json4s.{FieldSerializer, ShortTypeHints, DefaultFormats, Formats}
import org.json4s.native.Serialization

object FinkApiFormats {
  def apply() = {
    Serialization.formats(ShortTypeHints(List(classOf[Gallery], classOf[Post], classOf[Page], classOf[Category], classOf[Tag]))) +
      ShortTypeHints(List(classOf[Gallery], classOf[Post], classOf[Page], classOf[Category], classOf[Tag])) +
      FieldSerializer[Post]() + FieldSerializer[Page]() + FieldSerializer[Gallery]() + FieldSerializer[Image]()
  }
}
