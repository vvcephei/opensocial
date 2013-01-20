package org.vvcephei.opensocial.data

import xml.Node
import net.liftweb.json.{Extraction, Xml}
import net.liftweb.json.JsonAST.JValue
import net.liftweb.util.Helpers
import org.vvcephei.opensocial.uns.data.DAO
import com.google.inject.Singleton
import spray.httpx.unmarshalling.Unmarshaller
import spray.http.{HttpBody, MediaTypes}
import spray.util._

case class ContentKey(id: Option[String], key: Option[String], algorithm: Option[String], userId: Option[String])

object ContentKey {
  private implicit val formats = net.liftweb.json.DefaultFormats

  def apply(json: JValue) = Helpers.tryo {
    json.extract[ContentKey]
  }

  def unapply(json: JValue): Option[ContentKey] = apply(json)

  /**
   * The default unapply method for the case class.
   * We needed to replicate it here because we
   * have overloaded unapply methods
   */
  def unapply(in: Any) = {
    in match {
      case i: ContentKey => Some((i.id, i.key, i.algorithm, i.userId))
      case _ => None
    }
  }

  /**
   * Convert an contentKey to XML
   */
  implicit def toXml(contentKey: ContentKey): Node =
    <contentKey>
      {Xml.toXml(contentKey)}
    </contentKey>


  /**
   * Convert the contentKey to JSON format. This is
   * implicit and in the companion object, so
   * an ContentKey can be returned easily from a JSON call
   */
  implicit def toJson(contentKey: ContentKey): JValue =
    Extraction.decompose(contentKey)

  /**
   * Convert a Seq[ContentKey] to JSON format. This is
   * implicit and in the companion object, so
   * an ContentKey can be returned easily from a JSON call
   */
  implicit def toJson(contentKeys: Seq[ContentKey]): JValue =
    Extraction.decompose(contentKeys)

  /**
   * Convert a Seq[ContentKey] to XML format. This is
   * implicit and in the companion object, so
   * an ContentKey can be returned easily from an XML REST call
   */
  implicit def toXml(contentKeys: Seq[ContentKey]): Node =
    <contentKeys>
      {contentKeys.map(toXml)}
    </contentKeys>

  /**
   * Provide an implicit unmarshaller for spray
   */
  implicit val ContentKeyUnmarshaller = Unmarshaller[ContentKey](MediaTypes.`application/json`) {
    case HttpBody(contentType, buffer) =>
      apply(net.liftweb.json.parse(buffer.asString)).get
  }

  /**
   * Provide an implicit unmarshaller for spray
   */
  implicit val ContentKeysUnmarshaller = Unmarshaller[List[ContentKey]](MediaTypes.`application/json`) {
    case HttpBody(contentType, buffer) =>
      net.liftweb.json.parse(buffer.asString).extract[List[ContentKey]]
  }
}

trait ContentKeyDAO extends DAO[ContentKey] {
  def list(userId: String): Iterable[ContentKey]

  def list(userId: String, start: Int, limit: Int): Iterable[ContentKey] =
    list(userId).drop(start).take(limit)
}

@Singleton
class InMemoryContentKeyDAO extends ContentKeyDAO {
  val db = scala.collection.mutable.Map[String, ContentKey]()

  def list() = db.values

  def list(userId: String) = list().filter(_.userId == Some(userId))

  def find(s: String) = db.get(s)

  def remove(s: String) = db.remove(s)

  def add(contentKey: ContentKey) = contentKey.id.flatMap(id => {
    db(id) = contentKey
    find(id)
  })

  def update(id: String, update: ContentKey) = db.get(id) match {
    case None => None
    case Some(contentKey) =>
      val newKey = update.key match {
        case None => contentKey.key
        case x => x
      }
      val newAlgorithm = update.algorithm match {
        case None => contentKey.algorithm
        case x => x
      }
      val newUID = update.userId match {
        case None => contentKey.userId
        case x => x
      }
      update.id match {
        case None =>
          db(id) = ContentKey(Some(id), newKey, newAlgorithm, newUID)
          db.get(id)
        case Some(updateId) =>
          db(updateId) = ContentKey(Some(id), newKey, newAlgorithm, newUID)
          db.remove(id)
          db.get(updateId)
      }
  }
}
