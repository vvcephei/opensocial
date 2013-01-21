package org.vvcephei.opensocial.ui

import akka.actor.{ActorRef, Props, ActorSystem}
import spray.io.IOExtension
import spray.can.client.HttpClient
import spray.client.HttpConduit
import org.vvcephei.opensocial.uns.data.{FreesocialPersonData, Person}
import org.vvcephei.opensocial.data.{Content, ContentKey}
import HttpConduit._
import akka.dispatch.{Await, Future}
import akka.util.duration._
import org.vvcephei.opensocial.crypto.{EncryptResult, CryptoService}


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
  implicit val system = Http.system

  def main(args: Array[String]) {
    println("getting john's content")
    val (person, contents) = Await.result(personContent("john", 0, 3), 10 second)
    println(person)
    contents.foreach(c => println("monad: " + c))
    system.shutdown()
  }

  def personContent(personId: String, startIndex: Int = 0, limit: Int = Int.MaxValue): Future[(Person, List[Content])] =
    for {
      person <- Http.pipe[Person]("localhost:8080").apply(Get("/api/uns/users:/root/%s.json".format(personId)))
      (keyservers,peers) = person.freesocialData match {
        case None => (Nil,Nil)
        case Some(FreesocialPersonData(ko,po)) => (ko.getOrElse(Nil), po.getOrElse(Nil))
      }
      contentKeyses: List[List[ContentKey]] <- Future.sequence(keyservers.map(ks => Http.pipe[List[ContentKey]](ks).apply(Get("/api/keys/users/john/content?sortBy=date&sortDir=desc&start=%d&limit=%d".format(startIndex, limit)))))
      contentKeys = contentKeyses.flatten.flatMap(ck => ck.id.map(id => (id, ck))).toMap
      contentFutures = contentKeys.keys.toList.map(id => {
        val attempts = peers.map(peer => Http.pipe[Content]("localhost:8080").apply(Get("/api/contents/" + id)))
        Future.firstCompletedOf(attempts)
      })
      cipherContents <- Future.sequence(contentFutures)
    } yield {
      (person, cipherContents.map {
        case Content(Some(id), date, app, Some(data)) => contentKeys(id) match {
          case ContentKey(_, Some(key), Some(algorithm), _, _) =>
            Content(Some(id), date, app, Some(CryptoService.decrypt(EncryptResult(data, key, algorithm))))
          case _ =>
            Content(Some(id), date, app, None)
        }
      })
    }
}
