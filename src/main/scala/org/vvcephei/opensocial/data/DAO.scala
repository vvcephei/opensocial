package org.vvcephei.opensocial.uns.data

trait DAO[T] {
  def list(): Iterable[T]

  def find(s: String): Option[T]

  def remove(s: String): Option[T]

  def add(item: T): Option[T]

  def update(id: String, item: T): Option[T]
}

