package org.vvcephei.opensocial.uns.data

import xml.Node
import net.liftweb.json.{Extraction, Xml}
import net.liftweb.json.JsonAST.JValue

case class NameServer(id: Option[String], ip: Option[String])

object NameServer {
  private implicit val formats = net.liftweb.json.DefaultFormats

  /**
   * Convert an user to XML
   */
  implicit def toXml(nameServer: NameServer): Node =
    <nameServer>{Xml.toXml(nameServer)}</nameServer>


  /**
   * Convert the nameServer to JSON format. This is
   * implicit and in the companion object, so
   * an NameServer can be returned easily from a JSON call
   */
  implicit def toJson(nameServer: NameServer): JValue =
    Extraction.decompose(nameServer)

  /**
   * Convert a Seq[NameServer] to JSON format. This is
   * implicit and in the companion object, so
   * an NameServer can be returned easily from a JSON call
   */
  implicit def toJson(nameServers: Seq[NameServer]): JValue =
    Extraction.decompose(nameServers)

  /**
   * Convert a Seq[NameServer] to XML format. This is
   * implicit and in the companion object, so
   * an NameServer can be returned easily from an XML REST call
   */
  implicit def toXml(nameServers: Seq[NameServer]): Node =
    <nameServers>{
      nameServers.map(toXml)
      }</nameServers>


}

trait NameServerDAO {
  def find(s:String): Option[NameServer]
}

object InMemoryNameServerDAO extends NameServerDAO{
  val db = List(
    NameServer(Some("root"),Some("localhost:8080")),
    NameServer(Some("bar"),Some("localhost:8080"))
  ).map(n=>(n.id.getOrElse(""),n)).toMap

  def find(s:String) = db.get(s)
}
