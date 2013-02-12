import org.scalatra._
import javax.servlet.ServletContext

import fink.web._
import fink.data._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    Repositories.init

    context.mount(new Frontend, "/*")
    context.mount(new Admin, "/admin/*")
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    Repositories.shutdown
  }
}

