package com.softensity.controller.search

import akka.http.scaladsl.server.Route
import com.softensity.service.SearchService

object SearchModule {
  private val SearchEnginePath = "https://www.google.com/search"
  lazy val service: SearchService = new SearchService(SearchEnginePath)
  private lazy val controller: SearchController = new SearchController(service)
  lazy val routes: Route = controller.route
}
