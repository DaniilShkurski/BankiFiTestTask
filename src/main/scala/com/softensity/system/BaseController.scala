package com.softensity.system

import java.lang.reflect.InvocationTargetException

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{ContentTypeRange, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, PredefinedFromStringUnmarshallers, Unmarshaller}
import akka.util.ByteString
import org.json4s._

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContextExecutor

object BaseControllerProtocol extends Json4sSupport {

  implicit val serialization: Serialization = org.json4s.native.Serialization
  implicit val ec: ExecutionContextExecutor = TaskServer.executionContext

  val baseTypeFormats: Formats = DefaultFormats

  implicit val formats: Formats = baseTypeFormats
}

object CommonResponses {

  def apply[T](seq: Seq[T]) = CommonSuccessResponse[T](seq)

  def apply[T](obj: T) = CommonSuccessResponse[T](Seq(obj))

  case class CommonSuccessResponse[T](data: Seq[T] = Seq.empty[T])
}

trait BaseController extends Directives with PredefinedFromStringUnmarshallers {}

trait Json4sSupport {

  def unmarshallerContentTypes: Seq[ContentTypeRange] =
    List(`application/json`)

  private val jsonStringMarshaller =
    Marshaller.stringMarshaller(`application/json`)

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .mapWithCharset {
        case (ByteString.empty, _) => throw Unmarshaller.NoContentException
        case (data, charset)       => data.decodeString(charset.nioCharset.name)
      }

  implicit def unmarshaller[A: Manifest](
    implicit serialization: Serialization,
    formats: Formats
  ): FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller
      .map { s =>
        serialization.read[A](s)
      }
      .recover { _ => _ =>
        {
          case MappingException(_, ite: InvocationTargetException) =>
            throw ite.getCause
        }
      }

  implicit def marshaller[A <: AnyRef](
    implicit serialization: Serialization,
    formats: Formats
  ): ToEntityMarshaller[A] =
    jsonStringMarshaller.compose(serialization.write[A])

}
