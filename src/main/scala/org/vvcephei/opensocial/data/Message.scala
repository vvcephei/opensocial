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
import Util.newField

trait TupleBearing[T] {
  val tuple: T
}

trait Overridable[T] {
  def overridenWith(other: T): T
}

trait RequirementsBearing {
  val meetsRequirements: Boolean
}

trait ModelObject[T] {
  val id: Option[String]

  def withId(id: Option[String]): T
}


abstract class OSCompanion[D <: TupleBearing[T], T](xmlName: String, xmlSeqName: String)(implicit mf: scala.reflect.Manifest[D]) {
  private implicit val formats = net.liftweb.json.DefaultFormats

  def apply(json: JValue) = Helpers.tryo {
    json.extract[D](formats, mf)
  }

  def unapply(json: JValue): Option[D] = apply(json)

  /**
   * The default unapply method for the case class.
   * We needed to replicate it here because we
   * have overloaded unapply methods
   */
  def unapply(in: Any) = {
    in match {
      case i: D => Some(i.tuple)
      case _ => None
    }
  }

  /**
   * Convert to JSON format. This is
   * implicit and in the companion object, so
   * obj can be returned easily from a JSON call
   */
  implicit def toJson(obj: D): JValue =
    Extraction.decompose(obj)

  /**
   * Convert a Seq[] to JSON format. This is
   * implicit and in the companion object, so
   * objs can be returned easily from a JSON call
   */
  implicit def toJson(objs: Seq[D]): JValue =
    Extraction.decompose(objs)

  /**
   * Provide an implicit unmarshaller for spray
   */
  implicit val ContentUnmarshaller = Unmarshaller[D](MediaTypes.`application/json`) {
    case HttpBody(contentType, buffer) =>
      apply(net.liftweb.json.parse(buffer.asString)).get
  }

  // Might have to replace these to comply with Open Social
  /**
   * Convert to XML
   */
  implicit def toXml(obj: D): Node =
    <REPLACE_NAME>
      {Xml.toXml(obj)}
    </REPLACE_NAME>.copy(label = xmlName)

  /**
   * Convert a Seq[] to XML format. This is
   * implicit and in the companion object, so
   * objs can be returned easily from an XML REST call
   */
  implicit def toXml(objs: Seq[D]): Node =
    <REPLACE_NAME>
      {objs.map(toXml)}
    </REPLACE_NAME>.copy(label = xmlSeqName)

}


trait InMemoryDAO[T <: RequirementsBearing with ModelObject[T] with Overridable[T]] extends DAO[T] {
  val db = scala.collection.mutable.Map[String, T]()

  def list = db.values

  def find(s: String) = db.get(s)

  def remove(s: String) = db.remove(s)

  def add(obj: T) = {
    val safeObj: T = obj.withId(Some(UUID.randomUUID().toString))
    if (safeObj.meetsRequirements) {
      db(safeObj.id.get) = safeObj
      find(safeObj.id.get)
    } else {
      None
    }
  }

  def update(id: String, update: T) = find(id) match {
    case None => None
    case Some(current: T) =>
      val newT = current.overridenWith(update).withId(current.id)
      if (newT.meetsRequirements) {
        db(id) = newT
        Some(newT)
      } else {
        None
      }
  }
}


case class Message(id: Option[String],
                   appUrl: Option[String],
                   body: Option[String],
                   bodyId: Option[String],
                   collectionIds: Option[List[String]],
                   inReplyTo: Option[String],
                   recipients: Option[String],
                   replies: Option[String],
                   senderId: Option[String],
                   timeSent: Option[Date],
                   titleId: Option[String],
                   `type`: Option[String],
                   updated: Option[Date],
                   urls: Option[String])
  extends TupleBearing[(Option[String], Option[String], Option[String], Option[String], Option[List[String]],
    Option[String], Option[String], Option[String], Option[String], Option[Date], Option[String], Option[String],
    Option[Date], Option[String])]
  with Overridable[Message] with RequirementsBearing with ModelObject[Message] {

  def withId(newId: Option[String]) = copy(id = newId)

  lazy val meetsRequirements = id != None

  lazy val tuple = (id, appUrl, body,
    bodyId, collectionIds, inReplyTo, recipients,
    replies, senderId, timeSent, titleId, `type`, updated, urls)

  def overridenWith(other: Message) = Message(newField(id, other.id), newField(appUrl, other.appUrl),
    newField(body, other.body), newField(bodyId, other.bodyId), newField(collectionIds, other.collectionIds),
    newField(inReplyTo, other.inReplyTo), newField(recipients, other.recipients), newField(replies, other.replies),
    newField(senderId, other.senderId), newField(timeSent, other.timeSent), newField(titleId, other.titleId),
    newField(`type`, other.`type`), newField(updated, other.updated), newField(urls, other.urls)
  )
}

object Message extends OSCompanion[Message, (Option[String], Option[String], Option[String], Option[String],
  Option[List[String]], Option[String], Option[String], Option[String], Option[String], Option[Date], Option[String],
  Option[String], Option[Date], Option[String])](xmlName = "message", xmlSeqName = "messages") {}


trait MessageDAO extends DAO[Message]

@Singleton
class InMemoryMessageDAO extends MessageDAO with InMemoryDAO[Message]
