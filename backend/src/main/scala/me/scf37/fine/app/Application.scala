package me.scf37.fine.app

import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Properties

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import me.scf37.config3.Config3
import org.slf4j.LoggerFactory

import scala.util.Try

/**
  * Application main class template.
  * Assumptions:
  * - logback
  * - environment name is defined as `env` system or environment property, hostname by default
  */
abstract class Application {

  protected def appName: String

  protected def run(config: Config): Unit

  protected lazy val version: Option[Version] = Version(appName)

  protected def referenceConfig: Config =
  // ConfigFactory.defaultReference() includes system properties which we do not want
    ConfigFactory.parseResources("reference.conf")

  protected def applicationConfigKey(key: String): Boolean = key == appName || key.startsWith(appName + ".")

  // for testing
  private[app] def referenceConfig1 = referenceConfig


  def main(args: Array[String]): Unit = {

    val (config, log) = init(args)

    try {

      run(config)

    } catch {
      case e: Throwable => log.error("Startup failed", e)
    }
  }

  private def init(args: Array[String]): (Config, PubLogging) = {
    val env =
      Config3.parse(args).toOption.flatMap { argsConfig =>
        if (argsConfig.hasPath("env")) Some(argsConfig.getString("env")) else None
      }.getOrElse(environmentName)

    val envFileConfig = ConfigFactory.parseResourcesAnySyntax(s"$env/$appName")
    val fileConfig = ConfigFactory.parseResourcesAnySyntax(appName)

    val config = loadConfig(
      args = args,
      applicationConfigKey = applicationConfigKey,
      systemProperties = System.getProperties,
      envVariables = System.getenv(),
      envFileConfig = envFileConfig,
      fileConfig = fileConfig,
      reference = referenceConfig
    ).fold(error => {
      println(error)
      System.exit(1)
      ???
    }, identity[Config])

    val logCandidates = Seq(
      s"/data/conf/$env/logback.xml",
      s"/data/conf/logback.xml",
      s"$env/logback.xml",
      "logback.xml"
    )

    val (log, logConfigName) = initLogging(
      candidates = logCandidates
    )

    val versionRevision = version.map(_.build_revision.take(7)).getOrElse("?")
    val versionDate = version.map(_.build_timestamp).getOrElse("?")

    log.info(s"Starting $appName version $versionDate ($versionRevision)")
    if (logConfigName.isDefined) {
      log.info(s"Using log configuration ${logConfigName.get}")
    } else {
      log.error("No log confiration found! Candidates: " + logCandidates.mkString(", "))
    }
    log.info(s"Using environment $env")
    if (!envFileConfig.isEmpty) {
      log.info("Using configuration file " + envFileConfig.origin().resource())
    }
    if (!fileConfig.isEmpty) {
      log.info("Using configuration file " + fileConfig.origin().resource())
    }
    log.info("Loaded configuration:")
    log.info("\n" + Config3.printConfig(referenceConfig, config, applicationConfigKey, _.contains("password")).toString)

    config -> log
  }

  private def initLogging(candidates: Seq[String]): (PubLogging, Option[String]) = {

    def tryPath(path: String) = Files.exists(Paths.get(path))
    def tryCp(path: String) = Thread.currentThread().getContextClassLoader.getResourceAsStream(path) != null
    def tryConf(path: String) = tryPath(path) || tryCp(path)


    val path = System.getProperty("logback.configurationFile") match {
      case null => candidates.find(tryConf)
      case x => Some(x)
    }

    path.foreach { path =>
      System.setProperty("logback.configurationFile", path)
    }

    new PubLogging -> path
  }

  private[app] def loadConfig(
    args: Array[String],
    applicationConfigKey: String => Boolean,
    systemProperties: Properties,
    envVariables: java.util.Map[String, String],
    envFileConfig: Config,
    fileConfig: Config,
    reference: Config
  ): Either[String, Config] = {

    if ((args sameElements Array("-h")) || (args sameElements Array("--help"))) {
      return Left(help(reference, applicationConfigKey))
    }

    val result = try {
      for {
        cmdlineConfig <- Config3.parse(args).left.map(_.toString)
        environmentConfig = ConfigFactory.parseProperties({
          // ConfigFactory.systemEnvironment() does not parse dots in env param names
          val p = new Properties()
          envVariables.forEach((k, v) => p.put(k, v))
          p
        })
        propsConfig = ConfigFactory.parseProperties(systemProperties)
        config = cmdlineConfig
          .withFallback(propsConfig)
          .withFallback(environmentConfig)
          .withFallback(envFileConfig)
          .withFallback(fileConfig)
          .withFallback(reference)

        errors = Config3.validate(reference, config, applicationConfigKey)
        _ <- if (errors.nonEmpty) Left(errors.mkString("\n")) else Right( () )
      } yield {
        config
      }
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        Left("\nUnexpected error")
    }

    result.left.map(error => error + "\n\n" + help(reference, applicationConfigKey))
  }

  private[app] def help(referenceConf: Config, filterKey: String => Boolean): String = {
    val h = Config3.help(referenceConf, applicationConfigKey)
    val additionalHelp = Seq(
      "Environment name can be set by `env` parameter via -env, system property or env variable",
      "Parameters:"
    )
    h.copy(help = h.help ++ additionalHelp ).toString
  }

  private[app] def environmentName: String = {
    Option(System.getProperty("env"))
      .orElse(Option(System.getenv("env")))
      .orElse(Try(InetAddress.getLocalHost.getHostName).toOption)
      .getOrElse("default")
  }

  private class PubLogging  {
    val log = LoggerFactory.getLogger(classOf[Application])
    def info(msg: => String): Unit = log.info(msg)
    def error(msg: => String, ex: Throwable = null): Unit = log.error(msg, ex)
  }

}
