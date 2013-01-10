package org.vvcephei.opensocial.uns

import data._
import net.liftweb.http.rest.RestHelper
import net.liftweb.http._
import net.liftweb.json.Extraction
import net.liftweb.json.JsonAST.JString
import scala.Some

object UnsApi extends RestHelper {
  val nameServerDAO = InMemoryNameServerDAO
  val nameServerIdentity = "root" :: Nil
  val userDAO = InMemoryUserDAO

  case class CompletedPath(path: List[String], id: String)

  object CompletedPath {
    def unapply(path: List[String]): Option[CompletedPath] =
      if (path.startsWith(nameServerIdentity) && path.length == nameServerIdentity.length + 1)
        Some(CompletedPath(nameServerIdentity, path.last))
      else None

    /**
     * The default unapply method for the case class.
     * We needed to replicate it here because we
     * have overloaded unapply methods
     */
    def unapply(in: Any) = {
      in match {
        case i: CompletedPath => Some((i.path, i.id))
        case _ => None
      }
    }
  }

  case class PartialPath(processed: List[String], remaining: List[String])

  object PartialPath {
    def unapply(path: List[String]): Option[PartialPath] = {
      if (path.startsWith(nameServerIdentity))
        Some(PartialPath(nameServerIdentity, path.drop(nameServerIdentity.length)))
      else None
    }

    /**
     * The default unapply method for the case class.
     * We needed to replicate it here because we
     * have overloaded unapply methods
     */
    def unapply(in: Any) = {
      in match {
        case i: PartialPath => Some((i.processed, i.remaining))
        case _ => None
      }
    }
  }

  serve("api" / "uns" / "lookup" prefix {
    case JsonGet(CompletedPath(path), _) => userDAO.find(path.id).map(JsonResponse(_))
    case JsonGet(PartialPath(path), _) => JString("defer to other nameserver: %s".format(path)) //FIXME
  })

  serve("api" / "uns" / "users" prefix {
    case JsonGet(Nil, _) => Extraction.decompose(userDAO.list())

    case JsonGet(CompletedPath(path), _) => userDAO.find(path.id).map(JsonResponse(_))
    case JsonGet(PartialPath(path), _) => JString("defer to other nameserver: %s".format(path)) //FIXME

    case JsonDelete(CompletedPath(path), _) => userDAO.remove(path.id).map(JsonResponse(_))
    case JsonDelete(PartialPath(path), _) => JString("defer to other nameserver: %s".format(path)) //FIXME

    case JsonPut(CompletedPath(path), (User(user), _)) => userDAO.add(user.copy(id = Some(path.id))).map(JsonResponse(_))
    case JsonPut(PartialPath(path), _) => JString("defer to other nameserver: %s".format(path)) //FIXME

    case JsonPost(CompletedPath(path), (User(user), _)) => userDAO.update(path.id, user).map(JsonResponse(_))
    case JsonPost(PartialPath(path), _) => JString("defer to other nameserver: %s".format(path)) //FIXME
  })
}
