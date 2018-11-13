package me.scf37.jugz.module

import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton
import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.filter.Cors
import com.twitter.util.Future
import me.scf37.jugz.rest.ExceptionFilter
import me.scf37.jugz.rest.RequestIdFilter
import me.scf37.jugz.rest.Route
import me.scf37.jugz.rest.exception.NotFoundException

class WebModule extends Module {
  override def configure(binder: Binder): Unit = {}

  @Provides @Singleton
  def createService(route: Route): Service[Request, Response] = {
    new Cors.HttpFilter(Cors.UnsafePermissivePolicy)
      .andThen(new RequestIdFilter)
      .andThen(new ExceptionFilter())
      .andThen(Service.mk[Request, Response] { req =>
        route(req).getOrElse(Future exception new NotFoundException)
      })
  }
}
