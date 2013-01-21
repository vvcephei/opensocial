package org.vvcephei.opensocial.uns.data

import com.google.inject.Singleton
import org.vvcephei.opensocial.data.Util._

case class Name(familyName: Option[String],
                formatted: Option[String],
                givenName: Option[String],
                honorificPrefix: Option[String] = None,
                honorificSuffix: Option[String] = None,
                middleName: Option[String] = None)
  extends Overridable[Name] {
  def overridenWith(other: Name) = Name(
    newField(familyName, other.familyName),
    newField(formatted, other.formatted),
    newField(givenName, other.givenName),
    newField(honorificPrefix, other.honorificPrefix),
    newField(honorificSuffix, other.honorificSuffix),
    newField(middleName, other.middleName)
  )
}

case class FreesocialPersonData(freesocial_keyServers: Option[List[String]],
                                freesocial_peers: Option[List[String]])
  extends Overridable[FreesocialPersonData] {
  def overridenWith(other: FreesocialPersonData) = FreesocialPersonData(
    newField(freesocial_keyServers, other.freesocial_keyServers),
    newField(freesocial_peers, other.freesocial_peers)
  )
}

case class Person(id: Option[String],
                  name: Option[Name],
                  displayName: Option[String],
                  freesocialData: Option[FreesocialPersonData])
  extends TupleBearing[(Option[String], Option[Name], Option[String], Option[FreesocialPersonData])]
  with Overridable[Person] with RequirementsBearing with ModelObject[Person] {
  def overridenWith(other: Person) = Person(
    newField(id, other.id),
    newFieldRec(name, other.name),
    newField(displayName, other.displayName),
    newFieldRec(freesocialData, other.freesocialData)
  )

  def withId(newId: Option[String]) = copy(id = newId)

  val meetsRequirements = id.isDefined && displayName.isDefined
  val tuple = (id, name, displayName, freesocialData)
}


object Person extends OSCompanion[Person, (Option[String], Option[Name], Option[String], Option[FreesocialPersonData])]("person", "people")

trait PersonDAO extends DAO[Person]

@Singleton
class InMemoryPersonDAO extends PersonDAO with SettableIdInMemoryDAO[Person]

