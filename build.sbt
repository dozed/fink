name := "fink"

version := "1.0"

scalaVersion := "2.9.1"

resolvers ++= Seq(
	"Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
	"snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
	"releases"  at "http://oss.sonatype.org/content/repositories/releases")

seq(webSettings :_*)

scanInterval in Compile := 0

port in container.Configuration := 8080

// webappResources in Compile <+= (resourceManaged in Compile)(sd => sd / "coffee")

// seq(coffeeSettings: _*)

// (sourceDirectory in (Compile, CoffeeKeys.coffee)) <<= baseDirectory{ _ / "src" / "src" / "main" / "webapp" / "admin" / "app"}

// (resourceManaged in (Compile, CoffeeKeys.coffee)) <<= (resourceManaged in Compile)(sd => sd / "coffee2")

seq(jrebelSettings: _*)

jrebel.webLinks <++= webappResources in Compile

libraryDependencies ++= Seq(
	"org.scala-lang" % "scala-compiler" % "2.9.1",
	"org.specs2" %% "specs2" % "1.11" % "test",
	"ch.qos.logback" % "logback-classic" % "0.9.26",
	"org.eclipse.jetty" % "jetty-webapp" % "8.0.4.v20111024" % "container",
	"javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
	"joda-time" % "joda-time" % "1.6.2",
  "org.scalaquery" % "scalaquery_2.9.0-1" % "0.9.5",
  "com.h2database" % "h2" % "1.3.168",
	"org.scalatra" %% "scalatra-scalate" % "2.1.0.M1",
	"org.scalatra" %% "scalatra-auth" % "2.1.0.M1",
	"org.scalatra" %% "scalatra-lift-json" % "2.1.0.M1",
	"org.scalatra" %% "scalatra-fileupload" % "2.1.0.M1",
	"org.scalatra" %% "scalatra-specs2" % "2.1.0.M1" % "test",
	"org.codehaus.janino" % "janino" % "2.6.1",
	"commons-io" % "commons-io" % "2.0.1",
	"net.liftweb" %% "lift-json" % "2.4",
	"com.typesafe" % "config" % "0.5.0"
)
