package org.vvcephei.opensocial.ui.web.comet

import net.liftweb._
import http._
import util._
import org.vvcephei.opensocial.data.Content

/**
 * The screen real estate on the browser will be represented
 * by this component. When the component changes on the server
 * the changes are automatically reflected in the browser.
 */
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

  /**
   * Put the messages in the li elements and clear
   * any elements that have the clearable class.
   */
  def render = "li *" #> msgs.map(_.data.get) & ClearClearable
}