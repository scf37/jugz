package me.scf37.jugz.module

import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Provides
import com.google.inject.Singleton
import me.scf37.jugz.controller.ApiEndpoint
import me.scf37.jugz.rest.Route

class ApiModule extends Module {
  override def configure(binder: Binder): Unit = {

  }

  @Provides @Singleton
  def createRoute(api: ApiEndpoint): Route = {
    api.endpoint
  }
}
