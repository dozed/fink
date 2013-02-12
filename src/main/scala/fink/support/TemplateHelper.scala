package fink.support

import org.fusesource.scalate.servlet.ServletRenderContext._
import org.joda.time.format.DateTimeFormat
import fink.data.RepositorySupport

object TemplateHelper extends RepositorySupport {

  private val fmt = DateTimeFormat.forPattern("yyyy-MM-dd")

  def formatDate(date: Long) = {
    fmt.print(date)
  }

}