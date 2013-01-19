package org.vvcephei.opensocial.data

import xml.Node
import net.liftweb.json.{Extraction, Xml}
import net.liftweb.json.JsonAST.JValue
import net.liftweb.util.Helpers
import org.joda.time.{DateTimeZone, DateTime}
import java.util.{UUID, Date}
import org.vvcephei.opensocial.uns.data.DAO
import com.google.inject.Singleton

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


}

trait ContentDAO extends DAO[Content]

@Singleton
class InMemoryContentDAO extends ContentDAO {
  val db: scala.collection.mutable.Map[String, Content] =
    scala.collection.mutable.Map(List(
      Content(Some("3F2504E0-4F89-11D3-9A0C-0305E82C3301"), Some(new DateTime(0, DateTimeZone.UTC).toDate), Some("myapp"), Some("mydata1")),
      Content(Some("3F2504E0-4F89-11D3-9A0C-0305E82C3302"), Some(new DateTime(0, DateTimeZone.UTC).toDate), Some("myapp"), Some("mydata2")),
      Content(Some("3F2504E0-4F89-11D3-9A0C-0305E82C3303"), Some(new DateTime(0, DateTimeZone.UTC).toDate), Some("myapp"), Some("mydata3"))
    ).map(u => (u.id.getOrElse(""), u)): _*)

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
