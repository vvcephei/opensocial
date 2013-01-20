package org.vvcephei.opensocial.ui

import akka.actor.{ActorRef, Props, ActorSystem}
import spray.io.IOExtension
import spray.can.client.HttpClient
import spray.client.HttpConduit
import org.vvcephei.opensocial.uns.data.Person
import org.vvcephei.opensocial.data.{Content, ContentKey}
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
  def main(args: Array[String]) {
    println("getting john's content")

    for {
      person <- Http.pipe[Person]("localhost:8080").apply(Get("/api/uns/users:/root/john.json"))
      keyservers <- person.freesocialData.freesocial_keyServers
      keyserver <- keyservers
      contentKeys <- Http.pipe[List[ContentKey]](keyserver).apply(Get("/api/keys/users/john/content"))
      contentKey <- contentKeys
      peers <- person.freesocialData.freesocial_peers
      peer <- peers
      id <- contentKey.id
      content <- Http.pipe[Content](peer).apply(Get("/api/contents/" + id))
    } {
      println("unit: " + content)
    }

    val fres = for {
      person <- Http.pipe[Person]("localhost:8080").apply(Get("/api/uns/users:/root/john.json"))
    } yield {
      person
    }

    fres.onComplete(r => println(r))

  }
}
