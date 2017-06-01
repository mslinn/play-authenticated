package controllers

import models.User
import play.api.mvc.RequestHeader

object AuthHelper {

  def parseUserFromCookie(implicit request: RequestHeader): Option[User] = {
    request.session.get("email").flatMap(email => User.findByEmail(email))
  }

  def parseUserFromQueryString(implicit request: RequestHeader): Option[User] = {
    val query = request.queryString.map { case (k, v) => k -> v.mkString }
    val email = query.get("email")
    val password = query.get("password")
    (email, password) match {
      case (Some(e), Some(p)) => User.findByEmail(e).filter(user => user.checkPassword(p))
      case _ => None
    }
  }

  def parseUserFromRequest(implicit request: RequestHeader): Option[User] = {
    parseUserFromQueryString orElse parseUserFromCookie
  }
}
