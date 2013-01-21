package org.vvcephei.opensocial.data

import org.vvcephei.opensocial.uns.data._
import com.google.inject.Singleton
import java.util.Date
import Util.newField

case class ContentKey(id: Option[String], key: Option[String], algorithm: Option[String], userId: Option[String],
                      date: Option[Date])
  extends TupleBearing[(Option[String], Option[String], Option[String], Option[String], Option[Date])]
  with Overridable[ContentKey] with RequirementsBearing with ModelObject[ContentKey] {
  def overridenWith(other: ContentKey) = ContentKey(newField(id, other.id), newField(key, other.key),
    newField(algorithm, other.algorithm), newField(userId, other.userId), newField(date, other.date))

  def withId(newId: Option[String]) = copy(id = newId)

  val meetsRequirements = id != None
  val tuple = (id, key, algorithm, userId, date)
}

object ContentKey extends OSCompanion[ContentKey, (Option[String], Option[String], Option[String], Option[String], Option[Date])]("contentKey", "contentKeys")

trait ContentKeyDAO extends DAO[ContentKey] {
  def list(userId: String): Iterable[ContentKey]

  def list(userId: String, start: Int, limit: Int, sortBy: String, sortDir: String): Iterable[ContentKey] =
    list(userId).toList.sortWith((lt, gt) =>
      (sortBy, sortDir) match {
        case ("date", "asc") => lt.date.isDefined && gt.date.isDefined && (lt.date.get before gt.date.get)
        case ("date", "desc") => lt.date.isDefined && gt.date.isDefined && (lt.date.get after gt.date.get)
        case _ => true
      }
    ).drop(start).take(limit)
}

@Singleton
class InMemoryContentKeyDAO extends ContentKeyDAO with InMemoryDAO[ContentKey] {
  def list(userId: String) = list().filter(_.userId == Some(userId))

  override def add(contentKey: ContentKey) = contentKey.id.flatMap(id => {
    db(id) = contentKey
    find(id)
  })
}
