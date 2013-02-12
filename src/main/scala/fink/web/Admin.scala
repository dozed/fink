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
    contentType = formats("html")
    if (request.getPathInfo == null) {
      response.redirect(url("/") + "/")
      halt()
    }
    jade("/admin/index.jade", "layout" -> "/admin/layouts/default.jade")
  }

  notFound {
    halt(404, <h1>Not found.  Bummer.</h1>)
  }

}
