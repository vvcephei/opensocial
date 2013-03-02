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

object FreesocialClient {
  implicit val system = Http.system

  def getPerson(personPath: String): Future[Person] = Http.pipe[Person]("localhost:8080").apply(Get("/api/uns/users:%s.json".format(personPath)))

  def getContentKeys(keyservers: List[String], personId: String, startIndex: Int, limit: Int): Future[List[ContentKey]] = {
    Future.sequence(keyservers.map(ks => getContentKeys(ks, personId, startIndex, limit))).map(_.flatten)
  }

  def getContentKeys(ks: String, personId: String, startIndex: Int, limit: Int): Future[List[ContentKey]] = {
    Http.pipe[List[ContentKey]](ks)
      .apply(Get("/api/keys/users/%s/content?sortBy=date&sortDir=desc&start=%d&limit=%d"
      .format(personId, startIndex, limit)))
  }

  def getContent(peers: List[String], id: String): Future[Content] =
    Future.firstCompletedOf(peers.map(peer => Http.pipe[Content]("localhost:8080").apply(Get("/api/contents/" + id))))


  def friendContent(personPath: String, startIndex: Int = 0, limit: Int = Int.MaxValue)
  : Future[List[Future[List[(Person, Content)]]]] =
    for {
      person <- getPerson(personPath)
    } yield {
      for {
        friend <- (personPath :: person.friends.getOrElse(Nil))
      } yield {
        val friendPerson = getPerson(friend)
        for {
          future_friend <- friendPerson
          (_, future_updates) <- personContent(friend, startIndex, limit)
        } yield {
          future_updates.map(content => (future_friend, content))
        }
      }
    }

  def personContent(personPath: String, startIndex: Int = 0, limit: Int = Int.MaxValue): Future[(Person, List[Content])] =
    for {
      person <- getPerson(personPath)
      (keyservers, peers) = person.freesocialData match {
        case None => (Nil, Nil)
        case Some(FreesocialPersonData(ko, po)) => (ko.getOrElse(Nil), po.getOrElse(Nil))
      }
      contentKeyses: List[ContentKey] <- getContentKeys(keyservers, person.id.get, startIndex, limit)
      contentKeys = contentKeyses.flatMap(ck => ck.id.map(id => (id, ck))).toMap
      contentFutures = contentKeys.keys.toList.map(id => getContent(peers, id))
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

object CLI {

  import FreesocialClient._

  implicit val system = Http.system

  def main(args: Array[String]) {
    println("getting john's content")
    val (person, contents) = Await.result(personContent("/root/fred", 0, 3), 10 second)
    println(person)
    contents.foreach(c => println("monad: " + c))

    val future_updates = Await.result(friendContent("/root/john"), 10 second)
    for {
      friend <- future_updates
      update <- Await.result(friend, 10 second)
    } {
      println(update._1.id.get)
      println(update._2)
    }
    system.shutdown()
  }



}
