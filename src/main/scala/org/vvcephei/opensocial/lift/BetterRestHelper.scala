package org.vvcephei.opensocial.lift

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.Req

trait BetterRestHelper extends RestHelper {
  // require that json requests end with a .json suffix
  // it is optional be default, but requiring it makes the api clearer
  override protected def jsonResponse_?(in: Req): Boolean =
    in.path.suffix.equalsIgnoreCase("json") && super.jsonResponse_?(in)
}
