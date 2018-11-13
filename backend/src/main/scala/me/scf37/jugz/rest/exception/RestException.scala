package me.scf37.jugz.rest.exception

import com.twitter.finagle.http.Status

class RestException(
  val message: String,
  val description: String,
  val status: Status,
  val code: String,
  val cause: Throwable
) extends RuntimeException(message, cause) {
  override def getMessage: String = message + ": " + description
}
