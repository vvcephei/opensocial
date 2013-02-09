package org.vvcephei.opensocial.uns.data

import com.google.inject.Singleton
import org.vvcephei.opensocial.data.Util._
import java.util.Date

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

case class Account(domain: Option[String], username: Option[String], userId: Option[String])
  extends Overridable[Account] {
  def overridenWith(other: Account) = Account(
    newField(domain, other.domain),
    newField(username, other.username),
    newField(userId, other.userId)
  )
}

case class Address(building: Option[String], country: Option[String], floor: Option[String],
                   formatted: Option[String], latitude: Option[String], locality: Option[String],
                   longitude: Option[String], postalCode: Option[String], region: Option[String],
                   streetAddress: Option[String], `type`: Option[String])
  extends Overridable[Address] {
  def overridenWith(other: Address) = Address(
    newField(building, other.building),
    newField(country, other.country),
    newField(floor, other.floor),
    newField(formatted, other.formatted),
    newField(latitude, other.latitude),
    newField(locality, other.locality),
    newField(longitude, other.longitude),
    newField(postalCode, other.postalCode),
    newField(region, other.region),
    newField(streetAddress, other.streetAddress),
    newField(`type`, other.`type`)
  )
}

case class AppData(key: Option[String], value: Option[String]) extends Overridable[AppData] {
  def overridenWith(other: AppData) = AppData(
    newField(key, other.key),
    newField(value, other.value)
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
                  //                  account: Option[Account] = None,
                  //                  addresses: Option[List[Address]] = None,
                  //                  appData: Option[List[AppData]] = None,
                  //                  connected: Option[Boolean],
                  //                  contactPreference: Option[String],
                  displayName: Option[String],
                  emails: Option[List[String]] = None,
                  //                  location: Option[String],
                  name: Option[Name] = None,
                  phoneNumbers: Option[List[String]] = None,
                  photos: Option[List[String]] = None,
                  //                  relationships: Option[List[String]],
                  status: Option[String] = None,
                  //                  tags: Option[String],
                  thumbnailUrl: Option[String] = None,
                  updated: Option[Date] = None,
                  urls: Option[List[String]] = None,
                  freesocialData: Option[FreesocialPersonData] = None,
                  friends: Option[List[String]] = None)
  extends TupleBearing[(Option[String], Option[String], Option[List[String]], Option[Name], Option[List[String]],
    Option[List[String]], Option[String], Option[String], Option[Date], Option[List[String]], Option[FreesocialPersonData],
    Option[List[String]])]
  with Overridable[Person] with RequirementsBearing with ModelObject[Person] {
  def overridenWith(other: Person) = Person(
    newField(id, other.id),
    newField(displayName, other.displayName),
    newField(emails, other.emails),
    newFieldRec(name, other.name),
    newField(phoneNumbers, other.phoneNumbers),
    newField(photos, other.photos),
    newField(status, other.status),
    newField(thumbnailUrl, other.thumbnailUrl),
    newField(updated, other.updated),
    newField(urls, other.urls),
    newField(freesocialData, other.freesocialData),
    newField(friends, other.friends)
  )

  def withId(newId: Option[String]) = copy(id = newId)

  val meetsRequirements = id.isDefined && displayName.isDefined
  val tuple = (id, displayName, emails, name, phoneNumbers, photos, status, thumbnailUrl, updated, urls, freesocialData,
    friends)
}


object Person extends OSCompanion[Person, (Option[String], Option[String], Option[List[String]], Option[Name], Option[List[String]],
  Option[List[String]], Option[String], Option[String], Option[Date], Option[List[String]], Option[FreesocialPersonData],
  Option[List[String]])]("person", "people")

trait PersonDAO extends DAO[Person]

@Singleton
class InMemoryPersonDAO extends PersonDAO with SettableIdInMemoryDAO[Person]

