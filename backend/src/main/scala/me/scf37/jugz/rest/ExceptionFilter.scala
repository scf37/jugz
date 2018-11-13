package me.scf37.jugz.rest

import com.twitter.finagle.Service
import com.twitter.finagle.SimpleFilter
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import me.scf37.jugz.logging.Logging
import me.scf37.jugz.rest.exception.InternalServerErrorException
import me.scf37.jugz.rest.exception.RestException

class ExceptionFilter extends SimpleFilter[Request, Response] with RestRespond with Logging {

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    service(request).rescue {
      case e: RestException =>
        logHandledError("Request processing failed", e)
        respondRestError(e)

      case e: Throwable =>
        logUnexpectedError("Unhandled error while processing request", e)
        respondRestError(new InternalServerErrorException(e))
    }
  }

  private def respondRestError(exception: RestException): Future[Response] = {
    val response = RestError(
      message = exception.message,
      description = exception.description,
      code = exception.code
    )

    respond(
      exception.status,
      response
    )
  }

}

private case class RestError(
  message: String,
  description: String,
  code: String
)