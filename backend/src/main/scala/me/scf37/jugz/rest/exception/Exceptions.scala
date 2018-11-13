package me.scf37.jugz.rest.exception

import com.twitter.finagle.http.Status

import scala.util.control.NoStackTrace

class BadJsonException(message: String)
  extends RestException("Bad JSON in request body", message, Status.BadRequest, "bad_json", null)
    with NoStackTrace

class ValidationException(message: String)
  extends RestException("Invalid request", message, Status.BadRequest, "invalid_request", null)
    with NoStackTrace

class InternalServerErrorException(cause: Throwable)
  extends RestException("Internal Server Error", "", Status.BadRequest, "internal_error", cause)

class NotFoundException
  extends RestException("Not Found", "", Status.NotFound, "not_found", null)
    with NoStackTrace

class JugNoSolutionException
  extends RestException("No Solution", "", Status.BadRequest, "jug_no_solution", null)
    with NoStackTrace
