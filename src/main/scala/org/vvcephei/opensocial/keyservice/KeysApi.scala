package org.vvcephei.opensocial.keyservice

import net.liftweb.http.rest.RestHelper
import net.liftweb.json.Extraction
import org.vvcephei.opensocial.data.InMemoryContentKeyDAO
import com.google.inject.{Singleton, Inject}

@Singleton
class KeysApi @Inject()(contentKeyDAO: InMemoryContentKeyDAO) extends RestHelper {
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
      val sortBy = req.params.get("sortBy") match {
        case Some("date" :: _) => "date"
        case _ => "none"
      }
      val sortDir = req.params.get("sortDir") match {
        case Some("asc" :: _) => "asc"
        case Some("desc" :: _) => "desc"
        case _ => "desc"
      }
      Extraction.decompose(contentKeyDAO.list(userId, start, limit, sortBy, sortDir))
    }
  })

  serve("debug" / "keys" prefix {
    case JsonGet(Nil, _) => Extraction.decompose(contentKeyDAO.list())
  })
}
