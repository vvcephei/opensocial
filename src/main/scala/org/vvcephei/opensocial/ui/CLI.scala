package org.vvcephei.opensocial.ui

import akka.actor.{ActorRef, Props, ActorSystem}
import spray.io.IOExtension
import spray.can.client.HttpClient
import spray.client.HttpConduit
import org.vvcephei.opensocial.uns.data.Person
import org.vvcephei.opensocial.data.{Content, ContentKey}
import HttpConduit._
import akka.dispatch.{Await, Future}
import akka.util.duration._


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
    implicit val system = Http.system

    // Side-effect only (because the types are not correct for a monad)
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

    // Monadic implementation, but a bit clunky:
    val r = Http.pipe[Person]("localhost:8080").apply(Get("/api/uns/users:/root/john.json")).flatMap(p => {
      val kservs = p.freesocialData.freesocial_keyServers.getOrElse(Nil)
      val peers = p.freesocialData.freesocial_peers.getOrElse(Nil)
      val ckfuts = kservs.map(ks => Http.pipe[List[ContentKey]](ks).apply(Get("/api/keys/users/john/content")))
      Future.sequence(ckfuts).map(loCks => (loCks, peers))
    })

    val r2 = r.flatMap {
      case (loCks, peers) => {
        val ids: List[String] = loCks.flatMap(cks => cks.map(_.id)).filter(_.isDefined).map(_.get)
        val contents = ids.map(id => {
          val attempts = peers.map(peer => Http.pipe[Content]("localhost:8080").apply(Get("/api/contents/" + id)))
          Future.firstCompletedOf(attempts)
        })
        Future.sequence(contents)
      }
    }

    r2.onFailure({
      case _ => system.shutdown()
    }).onSuccess({
      case l =>
        for (c <- l) {
          println("nonblock: " + c)
        }
    })


    // Best of both worlds: monadic implementation in a for comprehension
    val monadRes = (for {
      person <- Http.pipe[Person]("localhost:8080").apply(Get("/api/uns/users:/root/john.json"))
      kservs = person.freesocialData.freesocial_keyServers.getOrElse(Nil)
      peers = person.freesocialData.freesocial_peers.getOrElse(Nil)
      loCks <- Future.sequence(kservs.map(ks => Http.pipe[List[ContentKey]](ks).apply(Get("/api/keys/users/john/content"))))
      ids: List[String] = loCks.flatMap(cks => cks.map(_.id)).filter(_.isDefined).map(_.get)
      contents = ids.map(id => {
        val attempts = peers.map(peer => Http.pipe[Content]("localhost:8080").apply(Get("/api/contents/" + id)))
        Future.firstCompletedOf(attempts)
      })
      content <- Future.sequence(contents)
    } yield {
      content
    })


    val result = Await.result(monadRes, 10 second)
    result.foreach(c => println("monad: " + c))
    system.shutdown()

  }
}
