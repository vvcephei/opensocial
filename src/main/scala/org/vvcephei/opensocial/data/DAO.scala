package org.vvcephei.opensocial.uns.data

import net.liftweb.json.JsonAST.JValue
import net.liftweb.util.Helpers
import net.liftweb.json.{Xml, Extraction}
import spray.httpx.unmarshalling.Unmarshaller
import spray.http.{HttpBody, MediaTypes}
import xml.Node
import java.util.UUID
import spray.util._


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

  /**
   * Provide an implicit unmarshaller for spray
   */
  implicit val ContentKeysUnmarshaller = Unmarshaller[List[D]](MediaTypes.`application/json`) {
    case HttpBody(contentType, buffer) =>
      net.liftweb.json.parse(buffer.asString).extract[List[D]]
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

trait DAO[T] {
  def list(): Iterable[T]

  def find(s: String): Option[T]

  def remove(s: String): Option[T]

  def add(item: T): Option[T]

  def update(id: String, item: T): Option[T]
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

trait SettableIdInMemoryDAO[T <: RequirementsBearing with ModelObject[T] with Overridable[T]] extends InMemoryDAO[T] {
  override def add(obj: T) = if (obj.meetsRequirements) {
    db(obj.id.get) = obj
    find(obj.id.get)
  } else {
    None
  }

  override def update(id: String, update: T) = find(id) match {
    case None => None
    case Some(current: T) =>
      val newT = current.overridenWith(update)
      if (newT.meetsRequirements) {
        db(id) = newT
        Some(newT)
      } else {
        None
      }
  }
}



