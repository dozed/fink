resolvers += "stefri" at "http://stefri.github.com/repo/snapshots"

// addSbtPlugin("com.github.stefri" % "sbt-antlr" % "0.2-SNAPSHOT")

libraryDependencies <+= sbtVersion(v => v match {
case "0.11.0" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.0-0.2.8"
case "0.11.1" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.1-0.2.10"
case "0.11.2" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.11"
})

resolvers += "Jawsy.fi M2 releases" at "http://oss.jawsy.fi/maven2/releases"

addSbtPlugin("fi.jawsy.sbtplugins" %% "sbt-jrebel-plugin" % "0.9.0")

addSbtPlugin("me.lessis" % "coffeescripted-sbt" % "0.2.2" from "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/me.lessis/coffeescripted-sbt/scala_2.9.1/sbt_0.11.2/0.2.2/jars/coffeescripted-sbt.jar")

// http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/me.lessis/coffeescripted-sbt/scala_2.9.1/sbt_0.11.0/0.2.2/jars/coffeescripted-sbt.jar
resolvers += Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
