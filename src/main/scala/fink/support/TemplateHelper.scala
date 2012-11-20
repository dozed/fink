package fink.support

import org.fusesource.scalate.servlet.ServletRenderContext._

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

}