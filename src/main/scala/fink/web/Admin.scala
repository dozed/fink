package fink.web

import fink.data._

import org.scalatra.scalate.ScalateSupport
import org.scalatra.ScalatraServlet

class Admin extends ScalatraServlet with ScalateSupport with RepositorySupport with MediaSupport with AuthenticationRoutes with ResourceRoutes {

  override implicit protected val jsonFormats = JsonFormats()

  before("""/api/.+""".r) {
    contentType = formats("json")
  }

  get("/") {
    if (request.getPathInfo == null) {
      response.redirect(url("/") + "/")
      halt()
    }
    contentType = formats("html")
    jade("/admin/index.jade", "layout" -> "/admin/layouts/default.jade")
  }

  notFound {
    contentType = null
    serveStaticResource() getOrElse halt(404, "Not found.")
  }

}
