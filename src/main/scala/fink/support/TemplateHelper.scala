package fink.support

import org.fusesource.scalate.servlet.ServletRenderContext._
import org.joda.time.format.DateTimeFormat

object TemplateHelper {

  def base(path: String) = {
    Config.webBase + path
  }

  def themeBase(path: String) = {
    //renderContext.uri("/themes/" + themeName + (if (path.startsWith("/")) "") + path)
    Config.webBase + "/themes/" + Config.theme + path
  }

  def adminBase(path: String) = {
    Config.webBase + "/fink-admin" + path
  }

  def adminAssets(path: String) = {
    Config.webBase + "/admin" + path
  }

  private val fmt = DateTimeFormat.forPattern("yyyy-MM-dd")

  def formatDate(date: Long) = {
    fmt.print(date)
  }

}