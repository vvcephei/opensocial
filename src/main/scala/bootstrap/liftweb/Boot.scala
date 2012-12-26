package bootstrap.liftweb

import net.liftweb.http.LiftRules
import org.vvcephei.opensocial.Api
import org.vvcephei.opensocial.uns.UnsApi

class Boot {
  def boot {
    LiftRules.statelessDispatchTable.append(Api)
    LiftRules.statelessDispatchTable.append(UnsApi)
  }
}
