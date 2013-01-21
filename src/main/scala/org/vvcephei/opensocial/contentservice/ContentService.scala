package org.vvcephei.opensocial.contentservice

import org.vvcephei.opensocial.data._
import org.vvcephei.opensocial.crypto.CryptoService
import com.google.inject.Inject
import scala.Some

class ContentService @Inject()(contentDAO: ContentDAO,
                               contentKeyDAO: ContentKeyDAO) {

  def add(userId: String, content: Content) = {
    val cipherData = content.data.flatMap(clear => Some(CryptoService.encrypt(clear)))
    val cipherText = cipherData.flatMap(result => Some(result.cipherText))
    val resultContent = contentDAO.add(content.copy(id = None, data = cipherText))
    resultContent match {
      case Some(Content(Some(id), date, _, _)) =>
        val resultKey =
          contentKeyDAO.add(ContentKey(
            id = Some(id),
            key = cipherData.flatMap(r => Some(r.key)),
            algorithm = cipherData.flatMap(r => Some(r.algorithm)),
            userId = Some(userId),
            date = date)
          )
        resultKey.flatMap(ignoreKey => resultContent)
      case None =>
        None
    }
  }
}
