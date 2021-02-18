package com.softensity.controller.search

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import com.softensity.system.BaseController
import com.softensity.service.SearchService

private[search] class SearchController(service: SearchService) extends BaseController {

  import com.softensity.system.BaseControllerProtocol._

  def searchResult(query: String): Route =
    get {
      onSuccess(service.search(query)) {
        case Some(response) =>  complete(StatusCodes.OK, response)
        case None =>  complete(StatusCodes.NotFound)
      }
    }

  lazy val route: Route =
    pathPrefix("search") {
      parameter("q".as[String]) { query =>
          searchResult(query)
      }
    }

}
