package me.scf37.fine.app

import java.util.Properties

import com.twitter.finagle.Http
import com.twitter.finagle.Service
import com.twitter.finagle.http.Method
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.twitter.server.AdminHttpServer.Route
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import com.twitter.util.Future
import com.typesafe.config.Config

import scala.util.Try

trait ServerConfig {
  def httpListenAddr: String
}

abstract class Server[Cfg <: ServerConfig](
  appName: String,
  config: Config,
  version: Option[Version],
  adminPort0: String = ":9990"
) extends TwitterServer {

  // parse twitter server flags
  adminPort.parse(adminPort0.toString)

  flag.getAll(false).foreach { f =>
    if (f != adminPort) f.parse()
  }

  override def failfastOnFlagsNotParsed: Boolean = true

  override protected[this] def parseArgs(args: Array[String]): Unit = {
  }

  override protected def routes: Seq[Route] = {
    super.routes :+ Route(
      path = "/admin/about",
      aboutPage,
      alias = "About",
      group = None,
      includeInIndex = true,
      method = Method.Get
    )
  }

  protected def parseConfig(c: Config): Cfg

  protected def start(c: Cfg): (Service[Request, Response])

  def main() {
    val (serverConfig, service) = init()


    val server = Http.server
      .withHttpStats
      .withLabel("http")
      .withStatsReceiver(statsReceiver)
      .withCompressionLevel(0)
      .withDecompression(false)
      .serve(serverConfig.httpListenAddr, service)

    onExit {
      server.close()
    }
    Await.ready(server)
  }

  protected def init(): (ServerConfig, Service[Request, Response]) = {

    val serverConfig = Try(parseConfig(config)).fold(err => {
      error("Failed to parse configuration: " + err.toString, err)
      System.exit(1)
      ???
    }, identity[Cfg])

    val service = start(serverConfig)

    serverConfig -> service
  }

 private val aboutPage: Service[Request, Response] = new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      val r = Response()
      val versionHtml = version.map { v =>
        val fields = Seq("Name" -> v.name,
          "Version" -> v.version,
          "Build date" -> v.build_timestamp,
          "Repository" -> v.scm_repository,
          "Revision" -> v.build_revision,
          "Last Commits" -> v.build_last_few_commits.mkString("<br>"))

        "<table>" + fields.map {case (k, v) => s"<tr><td>$k</td><td>$v</td></tr>"}.mkString + "</table>"
      }.getOrElse("<p>No version information available</p>")

      val p = new Properties()
      config.entrySet().forEach(kv => p.put(kv.getKey, config.getString(kv.getKey)))

      import scala.collection.JavaConverters._
      val configHtml = "<table>" + p.entrySet()
        .asScala
        .toSeq
        .map(kv => kv.getKey.toString -> kv.getValue.toString)
        .sortBy(_._1)
        .map({case (k, v) => s"<tr><td>$k</td><td>$v</td></tr>"}).mkString + "</table>"

      r.contentString = s"""
            <h2>$appName</h3>
            <h3>Version</h3>
            $versionHtml
            <h3>Configuration</h3>
            $configHtml
       """
      r.headerMap += "Content-Type" -> "text/html"
      Future value r
    }
  }
}