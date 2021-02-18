package com.softensity.service

import java.io.StringReader
import java.net.URLEncoder

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.softensity.service.SearchService.{GoogleResponse, SimilarResult}
import com.softensity.system.TaskServer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps
import scala.xml.Node

class SearchService(searchEnginePath: String)(
  implicit val executionContext: ExecutionContextExecutor =
    TaskServer.executionContext,
  implicit val actorSystem: ActorSystem = TaskServer.system
) {

  def search(query: String): Future[Option[GoogleResponse]] = {

    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    val uri = searchEnginePath + "?q=" + encodedQuery
    val request = HttpRequest(method = HttpMethods.GET, uri = uri)
    sendRequest(request)
  }

  private def sendRequest(request: HttpRequest) = {
    for {
      response <- Http().singleRequest(request)
      content <- Unmarshal(response.entity).to[String]
    } yield extractResult(content)
  }

  private def extractResult(content: String) = {
    val parserFactory = new org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl
    val parser = parserFactory.newSAXParser()
    val source = new org.xml.sax.InputSource(new StringReader(content))
    val adapter = new scala.xml.parsing.NoBindingFactoryAdapter
    val xml = adapter.loadXML(source, parser)
    val nodes = xml \\ "div" \ "div" \ "div"
    val searchXMLResults = nodes.filter(
      div => (div \ "@class" toString) == "ZINbbc xpd O9g5cc uUPGi"
    )
    parseResult(searchXMLResults(1))
  }

  private def parseResult(node: Node) = {
    val mainInfo = node \\ "div" filter (
      div => (div \ "@class" toString) == "kCrYT"
    )
    val headerInfoNodeOpt = mainInfo.headOption
    val similarInfoNodeOpt = mainInfo.tail.headOption
    for {
      similarInfoNode <- similarInfoNodeOpt
      headerInfoNode <- headerInfoNodeOpt
      (basicInfo, similarInfo) <- generateSimilarResults(similarInfoNode)
      response = generateResponse(headerInfoNode, similarInfo, basicInfo)
    } yield response

  }

  private def generateSimilarResults(node: Node) = {
    val info = node \ "div" \ "div" \ "div" \ "div" \ "div"
    info.headOption.map { head =>
      val child = head.child.headOption
      val similarInfo = (head \\ "span" \ "a")
      val similarResults = similarInfo.map(
        node =>
          SimilarResult(
            node \ "@href" toString,
            (node \ "span").head.child.head.toString
        )
      )
      (child.map(_.toString()), similarResults)
    }

  }

  private def generateResponse(node: Node,
                               similarInfo: Seq[SimilarResult],
                               basicInfo: Option[String]) = {
    val url = (node \ "a" \ "@href").toString
    val header = (node \ "div").head.child.head.toString()
    GoogleResponse(header, url, basicInfo, similarInfo)
  }


}

object SearchService {

  case class GoogleResponse(header: String,
                            url: String,
                            basicInfo: Option[String],
                            similarResults: Seq[SimilarResult])

  case class SimilarResult(url: String, name: String)
}