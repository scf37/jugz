package me.scf37.jugz.rest

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import me.scf37.jugz.rest.exception.BadJsonException
import me.scf37.jugz.rest.exception.ValidationException

import scala.reflect.ClassTag

object JsonHelper {
  private val objectMapper = {
    val om = new ObjectMapper().registerModule(DefaultScalaModule)
    om.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)
    om
  }

  def toJson(obj: Any): String =
    objectMapper.writeValueAsString(obj)

  def parseJson[T](json: String)(implicit ct: ClassTag[T]): T = {
    try {
      objectMapper.readValue(json, ct.runtimeClass.asInstanceOf[Class[T]])
    } catch {

      case e: JsonParseException =>
        throw new BadJsonException(e.getMessage)

      case e: JsonMappingException =>
        throw new ValidationException(e.getMessage)

    }
  }
}
