package me.scf37.jugz.rest

import com.twitter.finagle.Filter
import com.twitter.finagle.Service
import com.twitter.finagle.http.Method
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.twitter.util.Future

/**
  * Composable Route
  *
  */
trait Route extends (Request => Option[Future[Response]]) {

  def andThen(other: Route): Route = (r: Request) => this(r).orElse(other(r))

  def withFilter(f: Filter[Request, Response, Request, Response]): Route = new Route {

    override def apply(r: Request): Option[Future[Response]] = {
      this(r).map { response =>
        f.apply(r, Service.mk(_ => response))
      }
    }
  }
}

object Route {
  def get(path: String)(handler: Request => Future[Response]): Route =
    route(Method.Post, _ == path, handler)

  def post(path: String)(handler: Request => Future[Response]): Route =
    route(Method.Post, _ == path, handler)

  private def route(
    method: Method,
    pathMatcher: String => Boolean,
    handler: Request => Future[Response]
  ): Route = new Route {

    override def apply(r: Request): Option[Future[Response]] = {
      if (r.method == method && pathMatcher(r.path))
        Some(handler(r))
      else
        None
    }
  }
}
