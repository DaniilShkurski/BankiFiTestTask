package com.softensity.controller.math

import akka.http.scaladsl.server.Route
import com.softensity.service.MathService

object MathModule {
  lazy val service: MathService = new MathService()
  private lazy val controller: MathController = new MathController(service)
  lazy val routes: Route = controller.route
}