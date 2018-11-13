package me.scf37.jugz.rest

import com.twitter.finagle.http.Status
import com.twitter.finagle.http.Response
import com.twitter.util.Future

trait RestRespond {
  protected def respondOk(content: Any) = respond(Status.Ok, content)

  protected def respond(status: Status, content: Any): Future[Response] = {
    val r = Response(status)
    r.contentString = JsonHelper.toJson(content)
    r.contentType = "application/json"
    Future value r
  }
}
