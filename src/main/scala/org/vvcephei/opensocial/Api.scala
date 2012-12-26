package org.vvcephei.opensocial

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{GetRequest, Req}
import net.liftweb.json.JsonAST.JString

object Api extends RestHelper{
  serve {
    case Req(List("api","test"), "json", GetRequest) => JString("static test")
  }
}
