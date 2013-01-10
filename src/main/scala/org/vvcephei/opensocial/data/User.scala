package org.vvcephei.opensocial.uns.data

import xml.Node
import net.liftweb.json.{Extraction, Xml}
import net.liftweb.json.JsonAST.JValue
import net.liftweb.util.Helpers

case class User(id: Option[String],
                name: Option[String],
                keyServers: Option[List[String]],
                peers: Option[List[String]])

object User {
  private implicit val formats = net.liftweb.json.DefaultFormats

  def apply(json: JValue) = Helpers.tryo {
    json.extract[User]
  }

  def unapply(json: JValue): Option[User] = apply(json)

  /**
   * The default unapply method for the case class.
   * We needed to replicate it here because we
   * have overloaded unapply methods
   */
  def unapply(in: Any) = {
    in match {
      case i: User => Some((i.id, i.name, i.keyServers, i.peers))
      case _ => None
    }
  }

  /**
   * Convert an user to XML
   */
  implicit def toXml(user: User): Node =
    <user>
      {Xml.toXml(user)}
    </user>


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
    <users>
      {users.map(toXml)}
    </users>


}

object InMemoryUserDAO extends DAO[User] {
  val db: scala.collection.mutable.Map[String, User] =
    scala.collection.mutable.Map(List(User(Some("3F2504E0-4F89-11D3-9A0C-0305E82C3301"), Some("John"), Some(Nil), Some(Nil))).map(u => (u.id.getOrElse(""), u)): _*)

  def list() = db.values

  def find(s: String) = db.get(s)

  def remove(s: String) = db.remove(s)

  def add(user: User) = user.id.flatMap(id => {
    db.get(id) match {
      case None =>
        db(id) = user
        find(id)
      case _ => None
    }
  })

  def update(id: String, update: User) = db.get(id) match {
    case None => None
    case Some(user) =>
      val newName = update.name match {
        case None => user.name
        case x => x
      }
      val newKS = update.keyServers match {
        case None => user.keyServers
        case x => x
      }
      val newPeers = update.peers match {
        case None => user.peers
        case x => x
      }
      update.id match {
        case None =>
          db(id) = User(Some(id), newName, newKS, newPeers)
          db.get(id)
        case Some(updateId) =>
          db(updateId) = User(Some(id), newName, newKS, newPeers)
          db.remove(id)
          db.get(updateId)
      }
  }
}
