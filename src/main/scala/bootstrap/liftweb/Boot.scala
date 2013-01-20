package bootstrap.liftweb

import net.liftweb.http.LiftRules
import org.vvcephei.opensocial.Api
import org.vvcephei.opensocial.keyservice.KeysApi
import org.vvcephei.opensocial.contentservice.ContentApi
import com.google.inject.{Injector, Guice}
import org.vvcephei.opensocial.injection.FreesocialModule
import org.vvcephei.opensocial.uns.data._
import org.vvcephei.opensocial.uns.data.Name
import org.vvcephei.opensocial.uns.data.FreesocialPersonData
import org.vvcephei.opensocial.uns.UnsApi
import scala.Some
import org.vvcephei.opensocial.data.{Content, InMemoryContentDAO, ContentKey, InMemoryContentKeyDAO}
import org.joda.time.{DateTimeZone, DateTime}

class Boot {
  def populatePeople(injector: Injector) {
    val personDAO = injector.getInstance(classOf[InMemoryPersonDAO])
    personDAO.add(Person(
      "john",
      Name("Roesler", "John Roesler", "John"),
      Some("john"),
      FreesocialPersonData(Some("localhost:8080"::Nil), Some("localhost:8080"::Nil))
    ))
    println(personDAO.list())
  }

  def populateNameServers(injector: Injector) {
    val serverDAO = injector.getInstance(classOf[InMemoryNameServerDAO])
    serverDAO.add(NameServer(Some("root"), Some("localhost:8080")))
  }

  def populateContent(injector: Injector) {
    val contentDAO = injector.getInstance(classOf[InMemoryContentDAO])
    val contentKeyDAO = injector.getInstance(classOf[InMemoryContentKeyDAO])

    val Some(Content(Some(id1),_,_,_)) = contentDAO.add(Content(None, Some(new DateTime(0, DateTimeZone.UTC).toDate), Some("myapp"), Some("mydata1")))
    contentKeyDAO.add(ContentKey(Some(id1), Some("key"), Some("noop"), Some("john")))
    val Some(Content(Some(id2),_,_,_)) = contentDAO.add(Content(None, Some(new DateTime(0, DateTimeZone.UTC).toDate), Some("myapp"), Some("mydata1")))
    contentKeyDAO.add(ContentKey(Some(id2), Some("key"), Some("noop"), Some("john")))
    val Some(Content(Some(id3),_,_,_)) = contentDAO.add(Content(None, Some(new DateTime(0, DateTimeZone.UTC).toDate), Some("myapp"), Some("mydata1")))
    contentKeyDAO.add(ContentKey(Some(id3), Some("key"), Some("noop"), Some("john")))
  }

  def boot() {
    val identity = ("root" :: Nil).mkString("/")
    val injector = Guice.createInjector(new FreesocialModule(identity))
    populatePeople(injector)
    populateNameServers(injector)
    populateContent(injector)

    LiftRules.statelessDispatchTable.append(Api)
    LiftRules.statelessDispatchTable.append(injector.getInstance(classOf[UnsApi]))
    LiftRules.statelessDispatchTable.append(injector.getInstance(classOf[KeysApi]))
    LiftRules.statelessDispatchTable.append(injector.getInstance(classOf[ContentApi]))
  }
}
