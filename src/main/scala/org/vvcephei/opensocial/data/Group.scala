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

case class Group(id: Option[String],
                 title: Option[String],
                 description: Option[String] = None) {
  require(id != null)
  require(title != null)
  require(description != null)

  lazy val meetsRequirements = id != None && title != None

  private lazy val tuple = (id, title, description)
}

object Group {
  private implicit val formats = net.liftweb.json.DefaultFormats

  def apply(json: JValue) = Helpers.tryo {
    json.extract[Group]
  }

  def unapply(json: JValue): Option[Group] = apply(json)

  /**
   * The default unapply method for the case class.
   * We needed to replicate it here because we
   * have overloaded unapply methods
   */
  def unapply(in: Any) = {
    in match {
      case i: Group => Some(i.tuple)
      case _ => None
    }
  }

  /**
   * Convert to XML
   */
  implicit def toXml(obj: Group): Node =
    <group>
      {Xml.toXml(obj)}
    </group>


  /**
   * Convert a Seq[] to XML format. This is
   * implicit and in the companion object, so
   * objs can be returned easily from an XML REST call
   */
  implicit def toXml(objs: Seq[Group]): Node =
    <groups>
      {objs.map(toXml)}
    </groups>

  /**
   * Convert to JSON format. This is
   * implicit and in the companion object, so
   * obj can be returned easily from a JSON call
   */
  implicit def toJson(obj: Group): JValue =
    Extraction.decompose(obj)

  /**
   * Convert a Seq[] to JSON format. This is
   * implicit and in the companion object, so
   * objs can be returned easily from a JSON call
   */
  implicit def toJson(objs: Seq[Group]): JValue =
    Extraction.decompose(objs)


  /**
   * Provide an implicit unmarshaller for spray
   */
  implicit val ContentUnmarshaller = Unmarshaller[Group](MediaTypes.`application/json`) {
    case HttpBody(contentType, buffer) =>
      apply(net.liftweb.json.parse(buffer.asString)).get
  }

}

trait GroupDAO extends DAO[Group]

@Singleton
class InMemoryGroupDAO extends GroupDAO {
  val db = scala.collection.mutable.Map[String, Group]()

  def list() = db.values

  def find(s: String) = db.get(s)

  def remove(s: String) = db.remove(s)

  def add(group: Group) = group match {
    case Group(None, title, description) => if (group.meetsRequirements) {
      val newG = Group(Some(UUID.randomUUID().toString), title, description)
      db(newG.id.get) = newG
      db.get(newG.id.get)
    } else {
      None
    }
  }

  def update(id: String, update: Group) = db.get(id) match {
    case None => None
    case Some(Group(_, title, description)) =>
      val newId = newField(Some(id), update.id).get
      val newG = Group(Some(newId), newField(title, update.title), newField(description, update.description))
      if (newG.meetsRequirements) {
        db(newId) = newG
        db.remove(id)
        db.get(newId)
      } else {
        None
      }
  }
}
