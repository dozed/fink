import sbt._
import Keys._

object ScalatralinkeddataBuild extends Build {
  val Organization = "fink"
  val Name = "fink"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.10.0"
  val ScalatraVersion = "2.2.0"

  import java.net.URL
  import com.github.siasia.PluginKeys.port
  import com.github.siasia.WebPlugin.{container, webSettings}

  lazy val project = Project (
    "fink",
    file("."),
    settings = Defaults.defaultSettings ++ webSettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      scalatraCrossVersion <<= (scalaVersion) {
        case v if v startsWith "2.9." => CrossVersion.Disabled
        case _ => CrossVersion.binary
      },
      libraryDependencies <<= (libraryDependencies, scalatraCrossVersion) {
        (libs, scalatraCV) => libs ++ Seq(
          "org.scalatra" % "scalatra" % ScalatraVersion cross scalatraCV,
          "org.scalatra" % "scalatra-scalate" % ScalatraVersion cross scalatraCV,
          "org.scalatra" % "scalatra-specs2" % ScalatraVersion % "test" cross scalatraCV,
          "org.scalatra" % "scalatra-json" % ScalatraVersion cross scalatraCV,
          "org.scalatra" % "scalatra-auth" % ScalatraVersion cross scalatraCV,
          "org.json4s"   %% "json4s-jackson" % "3.1.0",
          "com.typesafe.slick" %% "slick" % "1.0.0",
          "com.h2database" % "h2" % "1.3.166",
          "c3p0" % "c3p0" % "0.9.1.2",
          "org.fusesource.scalamd" %% "scalamd" % "1.6",
          // "org.codehaus.janino" % "janino" % "2.6.1",
          "commons-io" % "commons-io" % "2.0.1",
          // "com.typesafe" % "config" % "0.5.0"
          "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",
          "org.eclipse.jetty" % "jetty-webapp" % "8.1.8.v20121106" % "container",
          "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
        )
      },
      browseTask
    )
  )

  val scalatraCrossVersion = SettingKey[CrossVersion]("scalatra-cross-version", "cross build strategy for Scalatra")

  val browse = TaskKey[Unit]("browse", "open web browser to localhost on container:port")
  val browseTask = browse <<= (streams, port in container.Configuration) map { (streams, port) =>
    import streams.log
    val url = new URL("http://localhost:%s" format port)
    try {
      log info "Launching browser."
      java.awt.Desktop.getDesktop.browse(url.toURI)
    }
    catch {
      case _ => {
        log info { "Could not open browser, sorry. Open manually to %s." format url.toExternalForm }
      }
    }
  }
}
