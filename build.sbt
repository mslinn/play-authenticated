import play.sbt.routes.RoutesKeys.routesImport

name := "play-authenticated"
organization := "com.micronautics"
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))
version := "0.2.2"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(routesImport += "model.PlayUserIdBinders._")
  .settings(routesImport += "model.PlayIdBinders._")

scalaVersion := "2.11.11"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-target:jvm-1.8",
  "-unchecked",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Xlint"
)

scalacOptions in (Compile, doc) ++= baseDirectory.map {
  (bd: File) => Seq[String](
     "-sourcepath", bd.getAbsolutePath,
     "-doc-source-url", "https://github.com/mslinn/{name.value}/tree/master€{FILE_PATH}.scala"
  )
}.value

javacOptions ++= Seq(
  "-Xlint:deprecation",
  "-Xlint:unchecked",
  "-source", "1.8",
  "-target", "1.8",
  "-g:vars"
)

resolvers ++= List(
  "micronautics/play on bintray"  at "http://dl.bintray.com/micronautics/play",
  "micronautics/scala on bintray" at "http://dl.bintray.com/micronautics/scala"
)

libraryDependencies ++= Seq(
  "com.h2database"    %  "h2"              % "1.4.192" withSources(),
  "com.micronautics"  %% "has-id"          % "1.2.8"   withSources(),
  "com.micronautics"  %% "html-form-scala" % "0.1.9"   withSources(),
  "com.micronautics"  %% "html-email"      % "0.1.1"   withSources(),
  "net.codingwell"    %% "scala-guice"     % "4.1.0"   withSources(),
  "de.svenkubiak"     %  "jBCrypt"         % "0.4.1"   withSources(),
  "org.webjars"       %  "bootstrap"       % "3.3.7-1",
  "org.webjars"       %% "webjars-play"    % "2.5.0-4",
  "io.getquill"       %% "quill-jdbc"      % "1.2.1"   withSources(),
  specs2 % Test
)

logLevel := Level.Warn

// Only show warnings and errors on the screen for compilations.
// This applies to both test:compile and compile and is Info by default
logLevel in compile := Level.Warn

// Level.INFO is needed to see detailed output when running tests
logLevel in test := Level.Info

// define the statements initially evaluated when entering 'console', 'console-quick', but not 'console-project'
initialCommands in console := """
                                |""".stripMargin

cancelable := true
