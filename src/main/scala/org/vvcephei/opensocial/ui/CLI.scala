package org.vvcephei.opensocial.ui

import akka.actor.{Props, ActorSystem}
import spray.io.IOExtension
import spray.can.client.HttpClient
import spray.client.HttpConduit
import spray.http.{HttpResponse, HttpMethods, HttpRequest}
import akka.actor.Status.{Failure, Success}


object CLI {
  def main(args: Array[String]) {
    println("getting john's content")

    val targetHost = "www.iana.org"
    val targetPort = 80
    val targetPath = "/domains/example"

    implicit val system = ActorSystem()
    val ioBridge = IOExtension(system).ioBridge()
    val httpClient = system.actorOf(Props(new HttpClient(ioBridge)))
    val conduit = system.actorOf(props = Props(new HttpConduit(httpClient, targetHost, targetPort)))

    val pipeline = HttpConduit.sendReceive(conduit)

    val response = pipeline(HttpRequest(method = HttpMethods.GET, uri = targetPath))

    response.onComplete {
      case Right(response) =>
        println("result:")
        println(response.entity.asString)
        system.shutdown()
      case Left(error) =>
        println(error)
        system.shutdown()
    }
  }
}
