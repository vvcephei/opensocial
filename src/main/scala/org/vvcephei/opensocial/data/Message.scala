package org.vvcephei.opensocial.data

import java.util.Date
import org.vvcephei.opensocial.uns.data._
import com.google.inject.Singleton
import Util.newField

case class Message(id: Option[String],
                   appUrl: Option[String],
                   body: Option[String],
                   bodyId: Option[String],
                   collectionIds: Option[List[String]],
                   inReplyTo: Option[String],
                   recipients: Option[String],
                   replies: Option[String],
                   senderId: Option[String],
                   timeSent: Option[Date],
                   titleId: Option[String],
                   `type`: Option[String],
                   updated: Option[Date],
                   urls: Option[String])
  extends TupleBearing[(Option[String], Option[String], Option[String], Option[String], Option[List[String]],
    Option[String], Option[String], Option[String], Option[String], Option[Date], Option[String], Option[String],
    Option[Date], Option[String])]
  with Overridable[Message] with RequirementsBearing with ModelObject[Message] {

  def withId(newId: Option[String]) = copy(id = newId)

  lazy val meetsRequirements = id != None

  lazy val tuple = (id, appUrl, body,
    bodyId, collectionIds, inReplyTo, recipients,
    replies, senderId, timeSent, titleId, `type`, updated, urls)

  def overridenWith(other: Message) = Message(newField(id, other.id), newField(appUrl, other.appUrl),
    newField(body, other.body), newField(bodyId, other.bodyId), newField(collectionIds, other.collectionIds),
    newField(inReplyTo, other.inReplyTo), newField(recipients, other.recipients), newField(replies, other.replies),
    newField(senderId, other.senderId), newField(timeSent, other.timeSent), newField(titleId, other.titleId),
    newField(`type`, other.`type`), newField(updated, other.updated), newField(urls, other.urls)
  )
}

object Message extends OSCompanion[Message, (Option[String], Option[String], Option[String], Option[String],
  Option[List[String]], Option[String], Option[String], Option[String], Option[String], Option[Date], Option[String],
  Option[String], Option[Date], Option[String])](xmlName = "message", xmlSeqName = "messages") {}


trait MessageDAO extends DAO[Message]

@Singleton
class InMemoryMessageDAO extends MessageDAO with InMemoryDAO[Message]
