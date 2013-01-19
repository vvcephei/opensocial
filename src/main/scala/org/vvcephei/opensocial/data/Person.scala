package org.vvcephei.opensocial.uns.data

import xml.Node
import net.liftweb.json.{Extraction, Xml}
import net.liftweb.json.JsonAST.JValue
import net.liftweb.util.Helpers
import com.google.inject.Singleton

case class Name(familyName: String,
                formatted: String,
                givenName: String,
                honorificPrefix: Option[String] = None,
                honorificSuffix: Option[String] = None,
                middleName: Option[String] = None)

case class Person(id: String,
                  name: Name,
                  displayName: Option[String],
                  freesocialData: FreesocialPersonData) {
  require(id != null)
  require(name != null)
  require(displayName != null)
  require(freesocialData != null)
}


case class FreesocialPersonData(freesocial_keyServers: Option[List[String]],
                                freesocial_peers: Option[List[String]])

object Person {
  private implicit val formats = net.liftweb.json.DefaultFormats

  def apply(json: JValue) = Helpers.tryo {
    json.extract[Person]
  }

  def unapply(json: JValue): Option[Person] = apply(json)

  /**
   * The default unapply method for the case class.
   * We needed to replicate it here because we
   * have overloaded unapply methods
   */
  def unapply(in: Any) = {
    in match {
      case i: Person => Some((i.id, i.name, i.displayName, i.freesocialData))
      case _ => None
    }
  }

  /**
   * Convert an user to XML
   */
  implicit def toXml(user: Person): Node =
    <user>
      {Xml.toXml(user)}
    </user>


  /**
   * Convert the user to JSON format. This is
   * implicit and in the companion object, so
   * an Person can be returned easily from a JSON call
   */
  implicit def toJson(user: Person): JValue =
    Extraction.decompose(user)

  /**
   * Convert a Seq[Person] to JSON format. This is
   * implicit and in the companion object, so
   * an Person can be returned easily from a JSON call
   */
  implicit def toJson(users: Seq[Person]): JValue =
    Extraction.decompose(users)

  /**
   * Convert a Seq[Person] to XML format. This is
   * implicit and in the companion object, so
   * an Person can be returned easily from an XML REST call
   */
  implicit def toXml(users: Seq[Person]): Node =
    <users>
      {users.map(toXml)}
    </users>
}

trait PersonDAO extends DAO[Person]

@Singleton
class InMemoryPersonDAO extends PersonDAO {
  val db: scala.collection.mutable.Map[String, Person] = scala.collection.mutable.Map[String, Person]()

  def list() = db.values

  def find(s: String) = db.get(s)

  def remove(s: String) = db.remove(s)

  def add(user: Person) =
    db.get(user.id) match {
      case None =>
        db(user.id) = user
        find(user.id)
      case _ => None
    }


  def update(id: String, update: Person) = db.get(id) match {
    case None => None
    case Some(user) => None
    /*val newName = update.name match {
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
        db(id) = Person(Some(id), newName, newKS, newPeers)
        db.get(id)
      case Some(updateId) =>
        db(updateId) = Person(Some(id), newName, newKS, newPeers)
        db.remove(id)
        db.get(updateId)
    }*/
  }
}
