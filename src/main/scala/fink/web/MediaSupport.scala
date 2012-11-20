package fink.web

import org.scalatra.ScalatraBase
import fink.support.{Config, MediaManager}
import java.io.File

trait MediaSupport extends ScalatraBase {

  get("/uploads/images/:hash/:spec/:file") {
    (for {
      hash <- Option(params("hash"))
      image <- imageRepository.byHash(hash)
      ext <- MediaManager.imageExtensions.get(image.contentType)
      spec <- MediaManager.imageSpecs.filter(_.name == params("spec")).headOption
    } yield {
      val file = new File("%s/%s-%s.%s".format(Config.mediaDirectory, image.hash, spec.name, ext))
      if (!file.exists) halt(404)
      response.addHeader("Content-Disposition", "inline;filename=\"%s\"".format(image.filename))
      response.addHeader("Content-type", image.contentType)
      file
    }) getOrElse(halt(404))
  }

}
