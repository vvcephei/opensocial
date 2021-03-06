package org.vvcephei.opensocial.contentservice

import net.liftweb.http.rest.RestHelper
import net.liftweb.json.Extraction
import org.vvcephei.opensocial.data.InMemoryContentDAO
import org.vvcephei.opensocial.data.Content
import com.google.inject.Inject

class ContentApi @Inject()(contentDAO: InMemoryContentDAO,
                           contentService: ContentService) extends RestHelper {
  serve("api" / "contents" prefix {
    case JsonGet(id :: Nil, _) => Extraction.decompose(contentDAO.find(id))

    case JsonPost(Nil, (Content(content), request)) =>
      val user = request.param("user")
      val userId = user.get
      Extraction.decompose(contentService.add(userId, content))

    case JsonGet(Nil, _) => Extraction.decompose(contentDAO.list())
  })
}
