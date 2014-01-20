name := "gremlin-scala"

version := "2.4.0t-SNAPSHOT"

organization := "com.michaelpollmeier"

scalaVersion := "2.10.3"

//scalacOptions ++= Seq(
//  "-Xlog-implicits"
//  "-Ydebug"
//)

libraryDependencies <++= scalaVersion { scalaVersion =>
  val gremlinVersion = "2.4.0"
  Seq(
    "com.tinkerpop.gremlin" % "gremlin-java" % gremlinVersion,
    "com.tinkerpop" % "pipes" % gremlinVersion,
    "com.tinkerpop.blueprints" % "blueprints-graph-jung" % gremlinVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-graph-sail" % gremlinVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-sail-graph" % gremlinVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-neo4j-graph" % gremlinVersion % "provided",
    // "com.tinkerpop.blueprints" % "blueprints-neo4jbatch-graph" % gremlinVersion % "provided", //doesn't exist in snapshot repositories as of now... try again later
    "com.tinkerpop.blueprints" % "blueprints-orient-graph" % gremlinVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-dex-graph" % gremlinVersion % "provided",
    "com.tinkerpop.blueprints" % "blueprints-rexster-graph" % gremlinVersion % "provided",
    "com.chuusai" % "shapeless" % "2.0.0-M1" cross CrossVersion.full,
    // REPL dependencies
    "org.scala-lang" % "scala-library" % scalaVersion,
    "org.scala-lang" % "scala-compiler" % scalaVersion,
    "org.scala-lang" % "jline" % scalaVersion,
    // test dependencies
    "org.scalatest" %% "scalatest" % "2.0.RC2" % "test",
    "com.tinkerpop.gremlin" % "gremlin-test" % gremlinVersion % "test"
  )
}

resolvers ++= Seq(
  "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + "/.m2/repository",
  "Maven Central" at "http://repo1.maven.org/maven2/",
  "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Aduna Software" at "http://repo.aduna-software.org/maven2/releases/", //for org.openrdf.sesame
  "Restlet Framework" at "http://maven.restlet.org"
)

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

pomExtra := <url>https://github.com/mpollmeier/gremlin-scala</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:mpollmeier/gremlin-scala.git</url>
    <connection>scm:git:git@github.com:mpollmeier/gremlin-scala.git</connection>
  </scm>
  <developers>
    <developer>
      <id>mpollmeier</id>
      <name>Michael Pollmeier</name>
      <url>http://www.michaelpollmeier.com</url>
    </developer>
  </developers>

credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USER"), System.getenv("SONATYPE_PASS"))
