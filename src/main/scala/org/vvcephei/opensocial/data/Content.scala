package org.vvcephei.opensocial.data

import java.util.Date
import org.vvcephei.opensocial.uns.data._
import com.google.inject.Singleton
import Util.newField
import net.liftweb.json.Extraction

case class TextPost(title: String, body: List[String]) extends TupleBearing[(String, List[String])] {
  val tuple = (title,body)
}

object TextPost extends OSCompanion[TextPost, (String, List[String])]("textPost", "textPosts") {
  def registryKey = "textPost"
}

object AppRegistry {
  def extractor = Map(
    ("myapp", (s: String) => s),
    (TextPost.registryKey, (s: String) => TextPost.fromJsonString(s))
  )
}

case class Content(id: Option[String], date: Option[Date], app: Option[String], data: Option[String])
  extends TupleBearing[(Option[String], Option[Date], Option[String], Option[String])]
  with Overridable[Content] with RequirementsBearing with ModelObject[Content] {

  def overridenWith(other: Content) = Content(newField(id, other.id), newField(date, other.date), newField(app, other.app), newField(data, other.data))

  def withId(newId: Option[String]) = copy(id = newId)

  val meetsRequirements = id != None
  val tuple = (id, date, app, data)
}

object Content extends OSCompanion[Content, (Option[String], Option[Date], Option[String], Option[String])]("content", "contents")

trait ContentDAO extends DAO[Content]

@Singleton
class InMemoryContentDAO extends ContentDAO with InMemoryDAO[Content]
