package fink.web

import fink.data._
import fink.support._

import org.scalatra.fileupload.FileUploadSupport
import org.scalatra.scalate.ScalateSupport
import org.scalatra.{util, ScalatraServlet}

import java.io.File

class Admin extends ScalatraServlet with RepositorySupport with AuthenticationRoutes with ResourceRoutes with MediaSupport with ScalateSupport with FileUploadSupport {

  override implicit val jsonFormats = FinkApiFormats()

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
