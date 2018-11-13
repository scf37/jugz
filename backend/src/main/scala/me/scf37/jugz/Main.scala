package me.scf37.jugz

import com.typesafe.config.Config
import me.scf37.fine.app.Application
import me.scf37.fine.app.Version

object Main extends Application {

  override protected def appName: String = "jugz"

  override protected def run(config: Config): Unit = {

    val server = new WebServer(
      appName = appName,
      config = config,
      version = Version(appName),
      adminPort0 = config.getString("jugz.http.adminAddr")
    )

    server.main(Array())
  }

}