package com.softensity.controller.math

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import com.softensity.system.BaseController
import com.softensity.service.MathService

import scala.concurrent.duration._

private[math] class MathController(service: MathService) extends BaseController {
  import com.softensity.system.BaseControllerProtocol._

  def getPrimeNumbers(number: Int, methodOpt: Option[String]): Route =
    get {
      validate(
        number >= 2 && number < 100000,
        "number should be less than 100000 and bigger than"
      ) {
        methodOpt match {
          case Some("streaming") =>
            complete((StatusCodes.OK, service.streamingPrimeNumbers(number)))
          case None =>
            complete((StatusCodes.OK, service.functionalPrimeNumbers(number)))
          case Some(_) =>
            complete(StatusCodes.BadRequest)

        }
      }
    }

  lazy val route: Route =
    pathPrefix("primes") {
      pathPrefix(IntNumber) { number =>
        parameter("function".as[String] ?) { function =>
          pathEnd {
            withRequestTimeout(1.seconds) {
              getPrimeNumbers(number, function)
            }
          }
        }
      }
    }
}
