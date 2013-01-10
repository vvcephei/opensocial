package org.vvcephei.opensocial.data

import xml.Node
import net.liftweb.json.{Extraction, Xml}
import net.liftweb.json.JsonAST.JValue
import net.liftweb.util.Helpers
import org.vvcephei.opensocial.uns.data.DAO

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


}

trait ContentKeyDAO {
  def list(userId: String): Iterable[ContentKey]

  def list(userId: String, start: Int, limit: Int): Iterable[ContentKey] =
    list(userId).drop(start).take(limit)
}

object InMemoryContentKeyDAO extends DAO[ContentKey] with ContentKeyDAO {
  val db: scala.collection.mutable.Map[String, ContentKey] =
    scala.collection.mutable.Map(List(
      ContentKey(Some("3F2504E0-4F89-11D3-9A0C-0305E82C3301"), Some("key"), Some("noop"), Some("john")),
      ContentKey(Some("3F2504E0-4F89-11D3-9A0C-0305E82C3302"), Some("key"), Some("noop"), Some("john2")),
      ContentKey(Some("3F2504E0-4F89-11D3-9A0C-0305E82C3303"), Some("key"), Some("noop"), Some("john"))
    ).map(u => (u.id.getOrElse(""), u)): _*)

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
