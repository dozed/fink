import org.scalatra._
import javax.servlet.ServletContext

import fink.web._
import fink.support._
import fink.data._

import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    Repositories.init
    Repositories.db withSession {
      Query(SettingsTable).firstOption.foreach(Config.init)
    }

    context.mount(new Frontend, "/*")
    context.mount(new Admin, "/admin/*")
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    Repositories.shutdown
  }
}

