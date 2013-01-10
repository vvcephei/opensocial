package bootstrap.liftweb

import net.liftweb.http.LiftRules
import org.vvcephei.opensocial.Api
import org.vvcephei.opensocial.uns.UnsApi
import org.vvcephei.opensocial.keyservice.KeysApi
import org.vvcephei.opensocial.contentservice.ContentApi
import org.vvcephei.opensocial.data.{Content, InMemoryContentKeyDAO, InMemoryContentDAO}
import org.joda.time.DateTimeZone

class Boot {
  def boot() {
    val contentDao = InMemoryContentDAO
    val contentKeyDao = InMemoryContentKeyDAO


    LiftRules.statelessDispatchTable.append(Api)
    LiftRules.statelessDispatchTable.append(UnsApi)
    LiftRules.statelessDispatchTable.append(KeysApi)
    LiftRules.statelessDispatchTable.append(ContentApi)
  }
}
