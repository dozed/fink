package fink.web

import fink.data._

import org.scalatra.servlet.FileUploadSupport
import org.scalatra.scalate.ScalateSupport
import org.scalatra.ScalatraServlet

class Admin extends ScalatraServlet with ScalateSupport with RepositorySupport with MediaSupport with AuthenticationRoutes with ResourceRoutes {

  override implicit protected val jsonFormats = FinkApiFormats()

  before("""/api/.+""".r) {
    contentType = formats("json")
  }

  get("/") {
    contentType = formats("html")
    templateAttributes("layout") = ("/admin/layouts/default.jade")
    jade("/admin/index.jade")
  }

  notFound {
    halt(404, <h1>Not found.  Bummer.</h1>)
  }

}
