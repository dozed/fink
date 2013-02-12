package fink.support

import org.joda.time.format.DateTimeFormat
import java.util.Properties
import scala.collection.JavaConversions._
import fink.data._

object Config {
  
  val fmtDays = DateTimeFormat.forPattern("yyyy-MM-dd")
  val fmtMinutes = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")

  val properties = new Properties
  properties.load(this.getClass().getResourceAsStream("/fink.properties"))

  val theme = properties.getProperty("fink.frontend.theme")

  val mediaDirectory = properties.getProperty("fink.media.location")

}