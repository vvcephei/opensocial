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
import org.vvcephei.opensocial.data.{ContentKey, InMemoryContentKeyDAO}

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

  def populateContentKeys(injector: Injector) {
    val contentKeyDAO = injector.getInstance(classOf[InMemoryContentKeyDAO])
    contentKeyDAO.add(ContentKey(Some("3F2504E0-4F89-11D3-9A0C-0305E82C3304"), Some("key"), Some("noop"), Some("john")))
    contentKeyDAO.add(ContentKey(Some("3F2504E0-4F89-11D3-9A0C-0305E82C3305"), Some("key"), Some("noop"), Some("john")))
    contentKeyDAO.add(ContentKey(Some("3F2504E0-4F89-11D3-9A0C-0305E82C3306"), Some("key"), Some("noop"), Some("john")))
  }

  def boot() {
    val identity = ("root" :: Nil).mkString("/")
    val injector = Guice.createInjector(new FreesocialModule(identity))
    populatePeople(injector)
    populateNameServers(injector)
    populateContentKeys(injector)

    LiftRules.statelessDispatchTable.append(Api)
    LiftRules.statelessDispatchTable.append(injector.getInstance(classOf[UnsApi]))
    LiftRules.statelessDispatchTable.append(injector.getInstance(classOf[KeysApi]))
    LiftRules.statelessDispatchTable.append(injector.getInstance(classOf[ContentApi]))
  }
}
