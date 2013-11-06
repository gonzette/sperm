import AssemblyKeys._

assemblySettings

name := "hello"

scalaVersion := "2.10.2"

libraryDependencies ++= {
  Seq(
    "com.twitter" %% "finagle-core" % "6.6.2",
    "com.twitter" %% "finagle-ostrich4" % "6.6.2",
    "com.twitter" %% "finagle-http" % "6.6.2",
    "com.twitter" %% "util-core" % "6.6.0",
    "com.twitter" %% "util-logging" % "6.6.0"
  )
}

