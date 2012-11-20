name := "fink"

version := "1.0"

scalaVersion := "2.9.2"

resolvers ++= Seq(
  "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Releases"  at "http://oss.sonatype.org/content/repositories/releases")

seq(webSettings :_*)

scanInterval in Compile := 0

port in container.Configuration := 8080

// webappResources in Compile <+= (resourceManaged in Compile)(sd => sd / "coffee")

// seq(coffeeSettings: _*)

// (sourceDirectory in (Compile, CoffeeKeys.coffee)) <<= baseDirectory{ _ / "src" / "src" / "main" / "webapp" / "admin" / "app"}

// (resourceManaged in (Compile, CoffeeKeys.coffee)) <<= (resourceManaged in Compile)(sd => sd / "coffee2")

seq(jrebelSettings: _*)

jrebel.webLinks <++= webappResources in Compile

classpathTypes ~= (_ + "orbit")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.9.2",
  "org.specs2" %% "specs2" % "1.11" % "test",
  "ch.qos.logback" % "logback-classic" % "0.9.26",
  "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar")),
  "joda-time" % "joda-time" % "1.6.2",
  "org.scalaquery" % "scalaquery_2.9.0-1" % "0.9.5",
  "com.h2database" % "h2" % "1.3.168",
  "org.scalatra" % "scalatra-scalate" % "2.2.0-SNAPSHOT",
  "org.scalatra" % "scalatra-auth" % "2.2.0-SNAPSHOT",
  "org.scalatra" % "scalatra-json" % "2.2.0-SNAPSHOT",
  "org.json4s" %% "json4s-native" % "3.0.0",
  "org.scalatra" % "scalatra-specs2" % "2.2.0-SNAPSHOT" % "test",
  "org.fusesource.scalamd" % "scalamd" % "1.5",
  "org.codehaus.janino" % "janino" % "2.6.1",
  "commons-io" % "commons-io" % "2.0.1",
  "com.typesafe" % "config" % "0.5.0"
)
