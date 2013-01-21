package org.vvcephei.opensocial.uns.data

import xml.Node
import net.liftweb.json.{Extraction, Xml}
import net.liftweb.json.JsonAST.JValue
import com.google.inject.Singleton
import org.vvcephei.opensocial.data.Util.newField

case class NameServer(id: Option[String], ip: Option[String])
  extends TupleBearing[(Option[String], Option[String])]
  with Overridable[NameServer] with RequirementsBearing with ModelObject[NameServer] {
  def overridenWith(other: NameServer) = NameServer(newField(id, other.id), newField(ip, other.ip))

  def withId(newId: Option[String]) = copy(id = newId)

  val meetsRequirements = id.isDefined && ip.isDefined
  val tuple = (id, ip)
}

object NameServer extends OSCompanion[NameServer, (Option[String], Option[String])]("nameServer","nameServers")

trait NameServerDAO extends DAO[NameServer]

@Singleton
class InMemoryNameServerDAO extends NameServerDAO with InMemoryDAO[NameServer]
