package org.vvcephei.opensocial.ui

import akka.actor.{ActorRef, Props, ActorSystem}
import spray.io.IOExtension
import spray.can.client.HttpClient
import spray.client.HttpConduit
import org.vvcephei.opensocial.uns.data.Person
import org.vvcephei.opensocial.data.ContentKey
import HttpConduit._


object Http {
  private var _hostMap = Map[String, ActorRef]()

  implicit val system = ActorSystem()
  private val ioBridge = IOExtension(system).ioBridge()
  private val httpClient = system.actorOf(Props(new HttpClient(ioBridge)))

  private def _addHost(connectString: String) {
    println("adding " + connectString)
    val (host :: port :: Nil) = connectString.split(":").toList
    val conduit = system.actorOf(props = Props(new HttpConduit(httpClient, host, port.toInt)))
    _hostMap = _hostMap.updated(connectString, conduit)
  }

  def conduit(connectString: String) = if (_hostMap.contains(connectString)) {
    _hostMap(connectString)
  } else {
    _addHost(connectString)
    _hostMap(connectString)
  }


  def pipe[T: spray.httpx.unmarshalling.Unmarshaller](connectString: String) = {
    (sendReceive(conduit(connectString))
      ~> unmarshal[T])
  }
}

object CLI {

  def allKeys[U](hosts: List[String], path: String, onComplete: Either[Throwable, List[ContentKey]]) = hosts match {
    case Nil =>
    case h :: hs =>
  }


  def main(args: Array[String]) {
    println("getting john's content")

    Http.pipe[Person]("localhost:8080").apply(Get("/api/uns/users:/root/john.json"))
      .onSuccess({
      case person =>
        println(person)
        val kss = person.freesocialData.freesocial_keyServers.getOrElse(Nil)
        for (ks <- kss) {
          Http.pipe[List[ContentKey]](ks).apply(Get("/api/keys/users/john/content")).onFailure({
            case error =>
              println(error)
              Http.system.shutdown()
          }).onSuccess({
            case contentKeys => println(contentKeys)
          })
        }
      //        Http.system.shutdown()
    }).onFailure({
      case error =>
        println(error)
        Http.system.shutdown()
    })

    /*    pipeline(HttpRequest(method = HttpMethods.GET, uri = targetPath)).onComplete {
          case Right(response) =>
            println("result:")
            println(response.entity.asString)
            pipeline2(HttpRequest(method = HttpMethods.GET, uri = targetPath)).onComplete {
              case Right(response) =>
                println("result2:")
                println(response.entity.asString)
                Http.system.shutdown()
              case Left(error) =>
                println(error)
                Http.system.shutdown()
            }
          case Left(error) =>
            println(error)
            Http.system.shutdown()
        }*/
  }
}
