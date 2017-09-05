name := "macros"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % "2.2.5" % Test
)
// not needed in 2.11 as it is part of scala-reflect
//libraryDependencies += "org.scalamacros" %% "quasiquotes" % "2.1.0"

libraryDependencies ++= Seq(
  "org.typelevel" %% "macro-compat" % "1.1.1",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)
