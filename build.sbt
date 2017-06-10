name := "play-authenticated"

version := "0.1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  cache,
  ws,
  "com.h2database" %  "h2"         % "1.4.192" withSources(),
  "io.getquill"    %% "quill-jdbc" % "1.2.1"   withSources(),
  specs2 % Test
)
