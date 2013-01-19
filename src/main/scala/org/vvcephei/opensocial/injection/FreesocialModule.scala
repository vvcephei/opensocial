package org.vvcephei.opensocial.injection

import annotation.NameServerIdentity
import com.google.inject.AbstractModule
import org.vvcephei.opensocial.uns.data.{InMemoryNameServerDAO, NameServerDAO, InMemoryPersonDAO, PersonDAO}

class FreesocialModule(identity: String) extends AbstractModule {
  def configure() {
    bindConstant().annotatedWith(classOf[NameServerIdentity]).to(identity)
    bind(classOf[PersonDAO]).to(classOf[InMemoryPersonDAO])
    bind(classOf[NameServerDAO]).to(classOf[InMemoryNameServerDAO])
  }
}
