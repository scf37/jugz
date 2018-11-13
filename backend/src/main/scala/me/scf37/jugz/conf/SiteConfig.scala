package me.scf37.jugz.conf

import com.typesafe.config.Config
import me.scf37.fine.app.ServerConfig

class SiteConfig(c: Config)(
  val httpListenAddr: String = c.getString("jugz.http.addr"),
) extends ServerConfig
