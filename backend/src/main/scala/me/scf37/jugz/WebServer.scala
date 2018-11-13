package me.scf37.jugz

import com.google.inject.Guice
import com.google.inject.Key
import com.google.inject.TypeLiteral
import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.typesafe.config.Config
import me.scf37.fine.app.Server
import me.scf37.fine.app.Version
import me.scf37.jugz.conf.SiteConfig
import me.scf37.jugz.module.ApiModule
import me.scf37.jugz.module.WebModule


private class WebServer(
  appName: String,
  config: Config,
  version: Option[Version],
  adminPort0: String = ":9990"
) extends Server[SiteConfig](
  appName = appName,
  config = config,
  version = version,
  adminPort0 = adminPort0
) {

  override protected def parseConfig(c: Config): SiteConfig = {
    new SiteConfig(c)()
  }

  override protected def start(c: SiteConfig): Service[Request, Response] = {
    val service = Guice.createInjector(
      new ApiModule, new WebModule
    ).getInstance(Key.get(new TypeLiteral[Service[Request, Response]]{}))

    service
  }
}
