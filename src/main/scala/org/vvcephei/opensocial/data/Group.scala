package org.vvcephei.opensocial.data

import org.vvcephei.opensocial.uns.data._
import com.google.inject.Singleton
import Util.newField

case class Group(id: Option[String],
                 title: Option[String],
                 description: Option[String] = None)
  extends TupleBearing[(Option[String], Option[String], Option[String])]
  with Overridable[Group] with RequirementsBearing with ModelObject[Group] {

  def withId(newId: Option[String]) = copy(id = newId)

  lazy val meetsRequirements = id != None && title != None

  lazy val tuple = (id, title, description)

  def overridenWith(other: Group) = Group(newField(id, other.id), newField(title, other.title),
    newField(description, other.description))
}

object Group extends OSCompanion[Group,(Option[String], Option[String], Option[String])]("group","groups")

trait GroupDAO extends DAO[Group]

@Singleton
class InMemoryGroupDAO extends GroupDAO with InMemoryDAO[Group]
