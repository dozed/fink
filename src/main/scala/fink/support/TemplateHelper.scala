package fink.support

import org.fusesource.scalate.servlet.ServletRenderContext._
import org.joda.time.format._
import fink.data._

object TemplateHelper extends RepositorySupport {

  private val fmt = DateTimeFormat.forPattern("yyyy-MM-dd")
  private val yearFormat = DateTimeFormat.forPattern("yyyy")
  private val monthFormat = DateTimeFormat.forPattern("MM")
  private val dayFormat = DateTimeFormat.forPattern("dd")

  private val formats = collection.mutable.Map[String, DateTimeFormatter]()

  def formatDate(date: Long) = {
    fmt.print(date)
  }

  def formatDate(date: Long, fs: String) = {
    val fmt = formats.getOrElse(fs, {
      val x = DateTimeFormat.forPattern(fs)
      formats(fs) = x
      x
    })
    fmt.print(date)
  }

  def day(date: Long) = {
    dayFormat.print(date)
  }

  def month(date: Long) = {
    monthFormat.print(date)
  }

  def year(date: Long) = {
    yearFormat.print(date)
  }

  def postUri(post: Post) = {
    renderContext.uri("/%s/%s/%s/%s/".format(year(post.date), month(post.date), day(post.date), post.shortlink))
  }

  def slug(s: String) = {
    s.toLowerCase.replaceAll("""[^a-z0-9\s-]""", "")
      .replaceAll("""[\s-]+""", " ").trim
      .replaceAll("""\s""", "-")
  }

}