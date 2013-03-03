package bootstrap.liftweb

import net.liftweb.http.LiftRules
import org.vvcephei.opensocial.Api
import org.vvcephei.opensocial.keyservice.KeysApi
import org.vvcephei.opensocial.contentservice.ContentApi
import com.google.inject.{Injector, Guice}
import org.vvcephei.opensocial.injection.FreesocialModule
import org.vvcephei.opensocial.uns.data._
import org.vvcephei.opensocial.data._
import org.joda.time.{DateTimeZone, DateTime}
import net.liftweb.sitemap.{Menu, Loc, SiteMap}
import net.liftweb.sitemap.Loc.Link
import org.vvcephei.opensocial.uns.UnsApi
import net.liftweb.common.Full
import scala.Some
import org.vvcephei.opensocial.uns.data.Name
import org.vvcephei.opensocial.uns.data.FreesocialPersonData

class Boot {
  def populatePeople(injector: Injector) {
    val personDAO = injector.getInstance(classOf[InMemoryPersonDAO])
    personDAO.add(Person(
      id = Some("john"),
      displayName = Some("john"),
      name = Some(Name(Some("Roesler"), Some("John Roesler"), Some("John"))),
      freesocialData = Some(FreesocialPersonData(Some("localhost:8080" :: "localhost:8080" :: Nil), Some("localhost:8080" :: "localhost:8080" :: Nil))),
      friends = Some("/root/fred" :: Nil)
    ))
    personDAO.add(Person(
      id = Some("fred"),
      displayName = Some("fred"),
      name = Some(Name(Some("Basdf"), Some("Fred Basdf"), Some("Fred"))),
      freesocialData = Some(FreesocialPersonData(Some("localhost:8080" :: "localhost:8080" :: Nil), Some("localhost:8080" :: "localhost:8080" :: Nil))),
      friends = Some("/root/john" :: Nil)
    ))
    println(personDAO.list())
  }

  def populateNameServers(injector: Injector) {
    val serverDAO = injector.getInstance(classOf[InMemoryNameServerDAO])
    serverDAO.add(NameServer(Some("root"), Some("localhost:8080")))
  }

  val dateGen = {
    var date = new DateTime(0, DateTimeZone.UTC)
    () => {
      date = date.plusMinutes(1)
      date.toDate
    }
  }

  def populateContent(injector: Injector) {
    val contentDAO = injector.getInstance(classOf[InMemoryContentDAO])
    val contentKeyDAO = injector.getInstance(classOf[InMemoryContentKeyDAO])

    {
      val Some(Content(Some(id1), date1, _, _)) =
        contentDAO.add(Content(None, Some(dateGen()), Some("myapp"), Some("fredDatat1")))
      contentKeyDAO.add(ContentKey(Some(id1), Some("key"), Some("noop"), Some("fred"), date1))
    }
    {
      val Some(Content(Some(id1), date1, _, _)) =
        contentDAO.add(Content(None, Some(dateGen()), Some("myapp"), Some("Fred's content 2")))
      contentKeyDAO.add(ContentKey(Some(id1), Some("key"), Some("noop"), Some("fred"), date1))
    }
    def doJohn() = {
      {
        val post = TextPost("A quick post", "Donec ac leo eget est posuere ultricies. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus." :: Nil)
        val Some(Content(Some(id1), date1, _, _)) = contentDAO.add(Content(None, Some(dateGen()), Some(TextPost.registryKey), Some(TextPost.toJsonString(post))))
        contentKeyDAO.add(ContentKey(Some(id1), Some("key"), Some("noop"), Some("john"), date1))
      }
      {
        val body = """Ut vulputate felis nec lorem tempus suscipit. Vestibulum nec augue sit amet dui euismod posuere ut fermentum dui. Mauris arcu neque, blandit sit amet vulputate vehicula, feugiat non lectus. Sed placerat facilisis fringilla. Nullam ac est massa. Etiam suscipit arcu ac risus gravida vulputate. Integer dictum pharetra erat, non scelerisque velit interdum sit amet. Etiam sed leo aliquam tortor vehicula dictum. Nunc convallis tempor libero, vitae viverra orci vulputate eget. Ut molestie pretium purus, non dignissim massa ullamcorper sed. Proin ultrices faucibus metus id ultrices. Duis gravida mi condimentum sem auctor a egestas lectus ultrices. Nam tortor ante, elementum non hendrerit quis, elementum eu tortor.""" ::
          """Donec ac leo eget est posuere ultricies. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Suspendisse potenti. Donec cursus lacus et nisi luctus porta. Morbi mattis, est eu laoreet dignissim, tellus velit volutpat massa, non tempor orci ipsum in erat. Nunc vehicula consequat nisi, eu sagittis turpis gravida id. Ut ac mattis eros. Donec id purus quis orci aliquet laoreet at a dolor.""" ::
          """Curabitur gravida, metus id varius viverra, augue tellus gravida lacus, a commodo lectus sapien et nisi. Morbi lorem risus, fermentum nec pretium eget, rhoncus vel lacus. Pellentesque metus diam, volutpat id hendrerit id, accumsan id turpis. In id nibh elit. Ut faucibus augue in urna bibendum nec malesuada lorem egestas. Proin sit amet nibh ut ligula imperdiet semper. Proin mattis, odio vitae eleifend pellentesque, diam libero porta mi, vitae condimentum neque nisi id metus. Maecenas in pulvinar tortor. Vestibulum elementum lorem at orci hendrerit tincidunt eget ut nisi.""" ::
          Nil
        val post = TextPost("A longer post", body)
        val Some(Content(Some(id2), date2, _, _)) = contentDAO.add(Content(None, Some(dateGen()), Some(TextPost.registryKey), Some(TextPost.toJsonString(post))))
        contentKeyDAO.add(ContentKey(Some(id2), Some("key"), Some("noop"), Some("john"), date2))
      }
      {
        val body = """Donec ac leo eget est posuere ultricies. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Suspendisse potenti. Donec cursus lacus et nisi luctus porta. Morbi mattis, est eu laoreet dignissim, tellus velit volutpat massa, non tempor orci ipsum in erat. Nunc vehicula consequat nisi, eu sagittis turpis gravida id. Ut ac mattis eros. Donec id purus quis orci aliquet laoreet at a dolor.""" ::
          Nil
        val post = TextPost("A medium post", body)
        val Some(Content(Some(id2), date2, _, _)) = contentDAO.add(Content(None, Some(dateGen()), Some(TextPost.registryKey), Some(TextPost.toJsonString(post))))
        contentKeyDAO.add(ContentKey(Some(id2), Some("key"), Some("noop"), Some("john"), date2))
      }
      {
        val Some(Content(Some(id3), date3, _, _)) = contentDAO.add(Content(None, Some(dateGen()), Some("myapp"), Some("mydata1")))
        contentKeyDAO.add(ContentKey(Some(id3), Some("key"), Some("noop"), Some("john"), date3))
      }
    }
    doJohn()
    doJohn()
  }

  def setupWebUI() {
    LiftRules.addToPackages("org.vvcephei.opensocial.ui.web")
    // Build SiteMap
    val entries = List(
      Menu.i("Home") / "index", // the simple way to declare a menu
      Menu.i("John") / "users" / "john",

      // more complex because this menu allows anything in the
      // /static path to be visible
      Menu(Loc("Static", Link(List("static"), true, "/static/index"),
        "Static Content")))

    // set the sitemap. Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(entries: _*))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // Use HTML5 for rendering
    //    LiftRules.htmlProperties.default.set((r: Req) =>
    //      new Html5Properties(r.userAgent))

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

    setupWebUI()

  }
}
