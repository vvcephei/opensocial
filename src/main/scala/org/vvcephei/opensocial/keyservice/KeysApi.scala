package org.vvcephei.opensocial.keyservice

import net.liftweb.http.rest.RestHelper
import net.liftweb.json.Extraction
import org.vvcephei.opensocial.data.InMemoryContentKeyDAO

object KeysApi extends RestHelper {
  val contentKeyDAO = InMemoryContentKeyDAO

  serve("api" / "keys" / "users" prefix {
    case JsonGet(userId :: "content" :: Nil, req) => {
      val start = req.params.get("start") match {
        case Some(s :: _) => s.toInt
        case _ => 0
      }
      val limit = req.params.get("limit") match {
        case Some(s :: _) => s.toInt
        case _ => 10
      }
      Extraction.decompose(contentKeyDAO.list(userId, start, limit))
    }
  })

  serve("debug" / "keys" prefix {
    case JsonGet(Nil, _) => Extraction.decompose(contentKeyDAO.list())
  })
}
