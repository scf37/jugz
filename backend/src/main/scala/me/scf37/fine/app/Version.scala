package me.scf37.fine.app

import java.util.Properties

import scala.util.Try

/**
  * Version from build.properties
  * see https://github.com/scf37/sbt-build-properties
  *
  * @param version
  * @param build_revision
  * @param name
  * @param build_timestamp
  * @param scm_repository
  * @param build_last_few_commits
  */
case class Version(
  // maven version
  version: String,

  // SCM revision (git commit hash)
  build_revision: String,

  // application name
  name: String,

  // build ISO time (2018-08-26T12:01:27Z)
  build_timestamp: String,

  // remote repository, if any
  scm_repository: String,

  // last commits
  build_last_few_commits: Seq[String]
)

object Version {
  /**
    * Read build.properties from classpath.
    * There can be zero, one or multiple build.properties files on classpath.
    * "appName" is used to pick the right one - by "name" property in the file.
    *
    * @param appName app name in build.properties file to load
    * @param classLoader classloader to use
    * @return Version or None if anything happens
    */
  def apply(appName: String, classLoader: ClassLoader = Thread.currentThread().getContextClassLoader): Option[Version] = {
    loadVersion("build.properties", appName, classLoader)
  }

  private[app] def loadVersion(
    file: String,
    appName: String,
    classLoader: ClassLoader
  ): Option[Version] = {
    import scala.collection.JavaConverters._

    for {
      p <- classLoader.getResources(file).asScala
        .flatMap(url => Try(url.openStream()).toOption)
        .flatMap(is => Try {
            val p = new Properties()
            p.load(is)
            p
          }.toOption)
        .find(_.getProperty("name", "") == appName)
    } yield Version(
      version = p.getProperty("version", "?"),
      build_revision = p.getProperty("build_revision", "?"),
      name = p.getProperty("name", "?"),
      build_timestamp = p.getProperty("build_timestamp", "?"),
      scm_repository =  p.getProperty("scm_repository", "?"),
      build_last_few_commits = p.getProperty("build_last_few_commits", "?")
        .split("\n").toSeq
    )
  }
}
