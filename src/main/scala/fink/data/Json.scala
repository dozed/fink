package fink.data

import org.json4s.{FieldSerializer, ShortTypeHints, DefaultFormats, Formats}

object JsonFormats {
  def apply() = {
    DefaultFormats + ShortTypeHints(List(classOf[Gallery], classOf[Post], classOf[Page], classOf[Category], classOf[Tag])) +
      FieldSerializer[Post]() + FieldSerializer[Page]() + FieldSerializer[Gallery]() + FieldSerializer[Image]()
  }
}
