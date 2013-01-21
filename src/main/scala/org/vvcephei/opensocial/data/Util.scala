package org.vvcephei.opensocial.data

import org.vvcephei.opensocial.uns.data.Overridable

object Util {
  def newField[T](oldV: Option[T], newV: Option[T]) = newV match {
    case None => oldV
    case x => x
  }

  def newFieldRec[T <: Overridable[T]](oldV: Option[T], newV: Option[T]) = (oldV, newV) match {
    case (_, None) => oldV
    case (None, _) => newV
    case (Some(o),Some(u)) => Some(o.overridenWith(u))
  }
}
