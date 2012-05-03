name := "fink"

version := "1.0"

scalaVersion := "2.9.1"

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"

resolvers += "Neo4j Maven 2 Snapshot" at "http://m2.neo4j.org/content/repositories/snapshots"

resolvers += "Neo4j Maven 2 Releases" at "http://m2.neo4j.org/content/repositories/releases"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// web settings
seq(webSettings :_*)

scanInterval in Compile := 0

webappResources in Compile <+= (resourceManaged in Compile)(sd => sd / "coffee")

// sbt-brew
seq(coffeeSettings: _*)

seq(coffeeJadeSettings: _*)

(sourceDirectory in (Compile, BrewKeys.coffee)) <<= baseDirectory{ _ / "src" / "main" / "webapp" / "admin" / "app"}

(resourceManaged in (Compile, BrewKeys.coffee)) <<= (resourceManaged in Compile)(sd => sd / "coffee" / "admin" / "app")

(BrewKeys.bare in (Compile, BrewKeys.coffee)) := true

// add neo4j-scala sources
unmanagedSourceDirectories in Compile <+= baseDirectory{ _ / "3rdparty" / "neo4j-scala" / "src" / "main" / "scala"}

// jrebel settings
seq(jrebelSettings: _*)

jrebel.webLinks <++= webappResources in Compile

// dependencies
libraryDependencies ++= Seq(
	"org.scala-lang" % "scala-compiler" % "2.9.1",
	"ch.qos.logback" % "logback-classic" % "0.9.26",
	"org.eclipse.jetty" % "jetty-webapp" % "8.0.4.v20111024" % "container",
	"javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
	"joda-time" % "joda-time" % "1.6.2",
	"com.google.guava" % "guava" % "r09",
	"org.antlr" % "antlr" % "3.3",
	"com.fasterxml" % "aalto-xml" % "0.9.7",
	"org.codehaus.woodstox" % "wstx-asl" % "3.2.6",
	// "org.neo4j" % "neo4j-scala" % "0.2.0-SNAPSHOT",
	"org.neo4j" % "neo4j-rest-graphdb" % "1.6",
	"org.scalatra" %% "scalatra-scalate" % "2.1.0.M1",
	"org.scalatra" %% "scalatra-auth" % "2.1.0.M1",
	"org.scalatra" %% "scalatra-lift-json" % "2.1.0.M1",
	"org.scalatra" %% "scalatra-fileupload" % "2.1.0.M1",
	"org.scalatra" %% "scalatra-scalatest" % "2.1.0.M1" % "test",
	"com.tinkerpop.gremlin" % "gremlin-scala" % "1.6-SNAPSHOT",
	"org.apache.lucene" % "lucene-analyzers" % "3.5.0",
	"com.tinkerpop.blueprints" % "blueprints-neo4j-graph" % "1.3-SNAPSHOT",
	"org.codehaus.janino" % "janino" % "2.6.1",
	"commons-io" % "commons-io" % "2.0.1",
	"net.liftweb" %% "lift-json" % "2.4"
)
