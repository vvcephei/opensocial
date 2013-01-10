package org.vvcephei.opensocial.crypto

import sun.reflect.generics.reflectiveObjects.NotImplementedException

case class EncryptResult(cipherText: String, key: String, algorithm: String)

object CryptoService {
  def encrypt(data: String) = EncryptResult(data, "nokey", "noop")

  def decrypt(encryptResult: EncryptResult) = encryptResult match {
    case EncryptResult(text, key, "noop") =>
      text
    case _ => throw new NotImplementedException
  }
}
