package me.scf37.jugz.rest

import java.security.SecureRandom
import java.util.Base64

import com.twitter.finagle.Service
import com.twitter.finagle.SimpleFilter
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import me.scf37.jugz.logging.Logging

class RequestIdFilter extends SimpleFilter[Request, Response] {
  private val random: SecureRandom = new SecureRandom
  private val encoder: Base64.Encoder = Base64.getUrlEncoder.withoutPadding

  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    val ip = request.headerMap.get("X-Real-IP").getOrElse(request.remoteHost)
    val requestId = newRequestId
    Logging.remoteIp.let(ip) {
      Logging.requestId.let(requestId) {
        service(request).map { resp =>
          resp.headerMap += "X-Request-Id" -> requestId
          resp
        }
      }
    }
  }

  private def newRequestId: String = {
    val bytes = new Array[Byte](6)
    random.nextBytes(bytes)
    encoder.encodeToString(bytes)
  }
}
