package com.softensity
import akka.http.scaladsl.server.Directives

package object system extends Directives {
  type Route = akka.http.scaladsl.server.Route
}