package org.vvcephei.opensocial.ui.web.comet

import net.liftweb._
import http._
import org.vvcephei.opensocial.data.{TextPost, Content}
import util.ClearClearable

class ContentListener extends CometActor with CometListener {
  private var msgs: Vector[Content] = Vector() // private state

  /**
   * When the component is instantiated, register as
   * a listener with the ChatServer
   */
  def registerWith = ContentServer

  /**
   * The CometActor is an Actor, so it processes messages.
   * In this case, we're listening for Vector[String],
   * and when we get one, update our private state
   * and reRender() the component. reRender() will
   * cause changes to be sent to the browser.
   */
  override def lowPriority = {
    case v: Vector[Content] => msgs = v; reRender()
  }

  def render = {
    ".content" #> msgs.map(c => {
        val (title, body) = if (c.app == Some(TextPost.registryKey)) {
          val (t,b) = TextPost.fromJsonString(c.data.get).tuple
          (Some(t),b.map(s => (<p>{s}</p>)))
        } else {
          (None, <p>{c.data.get}</p>)
        }

        <div class="content">
          {if (title.isDefined)
          <h4>
            {title.get}
          </h4>
          }
          <h6>
            {c.date.get}
          </h6>

          {body}
        </div>
      }) & ClearClearable
  }
}