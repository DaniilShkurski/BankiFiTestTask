package com.softensity.system
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.softensity.controller.math.MathModule
import com.softensity.controller.search.SearchModule

import scala.concurrent.ExecutionContextExecutor

object TaskServer {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val Host = "localhost"
  val Port = 8080
  def main(args: Array[String]): Unit = {
    val route: Route = MathModule.routes ~ SearchModule.routes
    val bindingFuture = Http().bindAndHandle(route, Host, Port)

    println(s"Server is running on $Host:$Port")
    sys.addShutdownHook({
      bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())
    })

  }
}
