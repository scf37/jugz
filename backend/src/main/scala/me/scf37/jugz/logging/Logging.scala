package me.scf37.jugz.logging

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.http.Request
import com.twitter.util.Future
import com.twitter.util.Local
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.MarkerFactory

import scala.collection.mutable.ArrayBuffer

trait Logging {
  private val logger = LoggerFactory.getLogger(getClass)
  private val AUDIT = MarkerFactory.getMarker("AUDIT")

  def logInfo(msg: => String): Unit = {
    setMDC()
    logger.info(msg)
  }

  /**
    * Error that was correctly handled and therefore does not require much attention
    *
    * @param msg
    * @param e
    */
  def logHandledError(msg: => String, e: Throwable): Unit = {
    setMDC()
    logger.warn(msg, e)
  }

  /**
    * Error that is not expected, most likely bug, require investigation
    * @param msg
    * @param e
    */
  def logUnexpectedError(msg: => String, e: Throwable): Unit = {
    setMDC()
    logger.error(msg, e)
  }

  def logAuditCallback[T](request: Option[Request], name: String, params: (String, Any)*)(body: => T): T = {
    val time = System.nanoTime()

    def logSuccess() = {
      val buf = ArrayBuffer[(String, Any)]()
      buf += "name" -> name
      val duration = (System.nanoTime() - time)/10000 / 100f
      buf += "took" -> duration
      buf ++= params
      logAudit(request, buf: _*)
    }

    def logFailure(e: Throwable) = {
      val buf = ArrayBuffer[(String, Any)]()
      buf += "name" -> name
      val duration = (System.nanoTime() - time)/10000 / 100f
      buf += "took" -> duration
      buf ++= params
      buf += "error" -> e.toString
      logAudit(request, buf: _*)
    }

    try {
      val result = body

      result match {
        case r: Future[_] =>
          r.onSuccess(_ => logSuccess())
            .onFailure(logFailure)
            .asInstanceOf[T]
        case r =>
          logSuccess()
          r
      }
    } catch {
      case e: Throwable =>
        logFailure(e)
        throw e
    }
  }

  protected def logAudit(request: Option[Request], params: (String, Any)*) = {
    if (logger.isInfoEnabled()) {
      setMDC()
      val b = Map.newBuilder[String, String]
      params.foreach { case (k, v) =>
        // ES does not like dots in field names - treats left part as object
        b += k.replace(".", "_") -> v.toString.trim
      }
      request.foreach { request =>
        b += "ip" -> Logging.remoteIp().getOrElse("")
        b += "method" -> request.method.toString
        b += "path" -> request.path
      }
      val json = Logging.om.writeValueAsString(b.result())
      logger.info(AUDIT, json)
    }
  }

  private def setMDC(): Unit = {
    MDC.put("requestId", Logging.remoteIp().getOrElse("<none>") + "/" + Logging.requestId().getOrElse("<none>"))
  }
}

object Logging {
  val requestId = new Local[String]()
  val remoteIp = new Local[String]()

  private val om = new ObjectMapper().registerModule(DefaultScalaModule)
}