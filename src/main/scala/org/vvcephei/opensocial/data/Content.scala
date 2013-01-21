package org.vvcephei.opensocial.data

import xml.Node
import net.liftweb.json.{Extraction, Xml}
import net.liftweb.json.JsonAST.JValue
import net.liftweb.util.Helpers
import java.util.{UUID, Date}
import org.vvcephei.opensocial.uns.data.DAO
import com.google.inject.Singleton
import spray.httpx.unmarshalling.Unmarshaller
import spray.http.{HttpBody, MediaTypes}
import spray.util._

case class Content(id: Option[String], date: Option[Date], app: Option[String], data: Option[String])

object Content {
  private implicit val formats = net.liftweb.json.DefaultFormats

  def apply(json: JValue) = Helpers.tryo {
    json.extract[Content]
  }

  def unapply(json: JValue): Option[Content] = apply(json)

  /**
   * The default unapply method for the case class.
   * We needed to replicate it here because we
   * have overloaded unapply methods
   */
  def unapply(in: Any) = {
    in match {
      case i: Content => Some((i.id, i.date, i.app, i.data))
      case _ => None
    }
  }

  /**
   * Convert an content to XML
   */
  implicit def toXml(content: Content): Node =
    <content>
      {Xml.toXml(content)}
    </content>


  /**
   * Convert the content to JSON format. This is
   * implicit and in the companion object, so
   * an Content can be returned easily from a JSON call
   */
  implicit def toJson(content: Content): JValue =
    Extraction.decompose(content)

  /**
   * Convert a Seq[Content] to JSON format. This is
   * implicit and in the companion object, so
   * an Content can be returned easily from a JSON call
   */
  implicit def toJson(contents: Seq[Content]): JValue =
    Extraction.decompose(contents)

  /**
   * Convert a Seq[Content] to XML format. This is
   * implicit and in the companion object, so
   * an Content can be returned easily from an XML REST call
   */
  implicit def toXml(contents: Seq[Content]): Node =
    <contents>
      {contents.map(toXml)}
    </contents>

  /**
   * Provide an implicit unmarshaller for spray
   */
  implicit val ContentUnmarshaller = Unmarshaller[Content](MediaTypes.`application/json`) {
    case HttpBody(contentType, buffer) =>
      apply(net.liftweb.json.parse(buffer.asString)).get
  }

}

trait ContentDAO extends DAO[Content]

@Singleton
class InMemoryContentDAO extends ContentDAO {
  val db = scala.collection.mutable.Map[String, Content]()

  def list() = db.values

  def find(s: String) = db.get(s)

  def remove(s: String) = db.remove(s)

  def add(content: Content) = content.id match {
    case None =>
      val id = UUID.randomUUID().toString
      db(id) = Content(Some(id), content.date, content.app, content.data)
      find(id)
    case _ => None
  }

  def update(id: String, update: Content) = db.get(id) match {
    case None => None
    case Some(content) =>
      val newDate = update.date match {
        case None => content.date
        case x => x
      }
      val newApp = update.app match {
        case None => content.app
        case x => x
      }
      val newData = update.app match {
        case None => content.data
        case x => x
      }
      update.id match {
        case None =>
          db(id) = Content(Some(id), newDate, newApp, newData)
          db.get(id)
        case Some(updateId) =>
          db(updateId) = Content(Some(id), newDate, newApp, newData)
          db.remove(id)
          db.get(updateId)
      }
  }
}
