package controllers

import javax.inject.Inject
import models._
import models.dao.Users
import play.api.mvc.RequestHeader
import play.api.mvc.Security.AuthenticatedBuilder

class Authenticated @Inject() (users: Users) {
  def parseUserFromCookie(implicit request: RequestHeader): Option[User] = {
    request.session.get("email").flatMap(email => users.findByEmail(email))
  }

  def parseUserFromQueryString(implicit request: RequestHeader): Option[User] = {
    val query = request.queryString.map { case (k, v) => k -> v.mkString }
    val email = query.get("email")
    val password = query.get("password")
    (email, password) match {
      case (Some(e), Some(p)) => users.findByEmail(e).filter(user => user.checkPassword(p))
      case _ => None
    }
  }

  def parseUserFromRequest(implicit request: RequestHeader): Option[User] = {
    parseUserFromQueryString orElse parseUserFromCookie
  }

  object Authenticated extends AuthenticatedBuilder[User](req => parseUserFromRequest(req))
}
