package org.vvcephei.opensocial.ui.web.comet

import net.liftweb._
import http._
import actor._
import org.vvcephei.opensocial.ui.FreesocialClient
import org.vvcephei.opensocial.data.{TextPost, Content}
import java.util.Date

/**
 * A singleton that provides chat features to all clients.
 * It's an Actor so it's thread-safe because only one
 * message will be processed at once.
 */
object ContentServer extends LiftActor with ListenerManager {
  private var msgs: Vector[Content] = Vector()

  FreesocialClient.personContent("/root/john").onSuccess({
    case (person, contents) =>
      msgs = msgs ++ contents
      updateListeners()
  })

  /**
   * When we update the listeners, what message do we send?
   * We send the msgs, which is an immutable data structure,
   * so it can be shared with lots of threads without any
   * danger or locking.
   */
  def createUpdate = msgs

  def log(s: Any) = println("ContentServer: " + s.toString)

  /**
   * process messages that are sent to the Actor. In
   * this case, we're looking for Strings that are sent
   * to the ChatServer. We append them to our Vector of
   * messages, and then update all the listeners.
   */
  override def lowPriority = {
    case s: String =>
      /*msgs :+= s; */ updateListeners()
    case (s: String, m: String) =>
      log((s, Content(None, Some(new Date()), app = Some(TextPost.registryKey), data = Some(m))))
      FreesocialClient.addContent(s, Content(None, Some(new Date()), app = Some(TextPost.registryKey), data = Some(TextPost.toJsonString(TextPost("No Title", m::Nil)))))
        .onSuccess({
        case Some(content) =>
          var tmp = msgs
          msgs = msgs.+:(content)
          updateListeners()
      })
        .onComplete(outcome => {
        log(outcome)
      })
  }
}
