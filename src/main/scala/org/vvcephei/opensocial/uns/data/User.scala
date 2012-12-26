package org.vvcephei.opensocial.uns.data

import xml.Node
import net.liftweb.json.{Extraction, Xml}
import net.liftweb.json.JsonAST.JValue

case class User(id: Option[String], name: Option[String])

object User {
  private implicit val formats = net.liftweb.json.DefaultFormats

  /**
   * Convert an user to XML
   */
  implicit def toXml(user: User): Node =
    <user>{Xml.toXml(user)}</user>


  /**
   * Convert the user to JSON format. This is
   * implicit and in the companion object, so
   * an User can be returned easily from a JSON call
   */
  implicit def toJson(user: User): JValue =
    Extraction.decompose(user)

  /**
   * Convert a Seq[User] to JSON format. This is
   * implicit and in the companion object, so
   * an User can be returned easily from a JSON call
   */
  implicit def toJson(users: Seq[User]): JValue =
    Extraction.decompose(users)

  /**
   * Convert a Seq[User] to XML format. This is
   * implicit and in the companion object, so
   * an User can be returned easily from an XML REST call
   */
  implicit def toXml(users: Seq[User]): Node =
    <users>{
      users.map(toXml)
      }</users>


}

trait UserDAO {
  def find(s:String): Option[User]
}

object InMemoryUserDAO extends UserDAO{
  val db: Map[String,User] = List(User(Some("3F2504E0-4F89-11D3-9A0C-0305E82C3301"),Some("John"))).map(u=>(u.id.getOrElse(""),u)).toMap

  def find(s:String) = db.get(s)
}
