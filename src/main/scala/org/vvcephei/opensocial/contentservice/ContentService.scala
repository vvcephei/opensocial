package org.vvcephei.opensocial.contentservice

import org.vvcephei.opensocial.data.{ContentKey, InMemoryContentKeyDAO, InMemoryContentDAO, Content}
import org.vvcephei.opensocial.crypto.CryptoService

object ContentService {
  val contentDAO = InMemoryContentDAO
  val contentKeyDao = InMemoryContentKeyDAO

  def add(userId: String, content: Content) = {
    val cipherData = content.data.flatMap(clear => Some(CryptoService.encrypt(clear)))
    val cipherText = cipherData.flatMap(result => Some(result.cipherText))
    val resultContent = contentDAO.add(content.copy(id = None, data = cipherText))
    resultContent match {
      case Some(Content(Some(id), _, _, _)) =>
        val resultKey =
          contentKeyDao.add(ContentKey(
            id = Some(id),
            key = cipherData.flatMap(r => Some(r.key)),
            algorithm = cipherData.flatMap(r => Some(r.algorithm)),
            userId = Some(userId))
          )
        resultKey.flatMap(ignoreKey => resultContent)
      case None =>
        None
    }
  }
}
