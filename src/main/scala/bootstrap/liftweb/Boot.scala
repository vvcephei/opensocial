package bootstrap.liftweb

import net.liftweb.http.LiftRules
import org.vvcephei.opensocial.Api
import org.vvcephei.opensocial.uns.UnsApi
import org.vvcephei.opensocial.keyservice.KeysApi
import org.vvcephei.opensocial.contentservice.ContentApi
import com.google.inject.{Injector, Guice}
import org.vvcephei.opensocial.injection.FreesocialModule
import org.vvcephei.opensocial.uns.data._
import org.vvcephei.opensocial.uns.data.Name
import org.vvcephei.opensocial.uns.data.FreesocialPersonData
import org.vvcephei.opensocial.uns.UnsApi
import scala.Some

class Boot {
  def populatePeople(injector: Injector) {
    val personDAO = injector.getInstance(classOf[InMemoryPersonDAO])
    personDAO.add(Person("john", Name("Roesler", "John Roesler", "John"), Some("john"), FreesocialPersonData(Some(Nil), Some(Nil))))
    println(personDAO.list())
  }

  def populateNameServers(injector: Injector) {
    val serverDAO = injector.getInstance(classOf[InMemoryNameServerDAO])
    serverDAO.add(NameServer(Some("root"),Some("localhost:8080")))
  }

  def boot() {
    val identity = ("root" :: Nil).mkString("/")
    val injector = Guice.createInjector(new FreesocialModule(identity))
    populatePeople(injector)

    LiftRules.statelessDispatchTable.append(Api)
    LiftRules.statelessDispatchTable.append(injector.getInstance(classOf[UnsApi]))
    LiftRules.statelessDispatchTable.append(KeysApi)
    LiftRules.statelessDispatchTable.append(ContentApi)
  }
}
