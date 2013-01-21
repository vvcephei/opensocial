package org.vvcephei.opensocial.data

object Util {
  def newField[T](oldV: Option[T], newV: Option[T]) = newV match {
    case None => oldV
    case x => x
  }
}
