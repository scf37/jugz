lazy val scalaSettings = Seq(
    scalaVersion := "2.12.7",
    scalacOptions ++= compilerOptions
)

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-deprecation",
  "-language:implicitConversions",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xlint",
  "-language:_",
  "-Ypartial-unification"
)

lazy val dependencies = libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-http" % "18.8.0",
  "com.twitter" %% "finagle-stats" % "18.8.0",
  "com.twitter" %% "twitter-server" % "18.8.0",
  "com.twitter" %% "twitter-server-logback-classic" % "18.8.0",

  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.4",
  "com.typesafe" % "config" % "1.3.2",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.google.inject" % "guice" % "4.2.2"
)

lazy val testDependencies = libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5",
  "org.seleniumhq.selenium" % "selenium-java" % "3.11.0"
).map(_ % "test")

lazy val jugz = project.in(file("."))
    .enablePlugins(PackPlugin)
    .settings(packMain := Map("jugz" -> "me.scf37.jugz.Main"))
    .settings(scalaSettings)
    .settings(dependencies)
    .settings(testDependencies)
    .settings(resourceGenerators in Compile += buildProperties)

