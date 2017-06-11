package models

import javax.inject.Inject
import controllers.WebJarAssets
import models.dao.Users
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Unauthorized
import play.api.mvc.Security.AuthenticatedBuilder
import play.api.mvc.{RequestHeader, Result}

case class LoginData(userId: String, password: String)

case class SignupData(email: String, userId: String, password: String)

trait UnauthorizedHandler {
  /** Default value is the standard Play unauthorized page */
  def onUnauthorized: RequestHeader => Result =
    _ => Unauthorized(views.html.defaultpages.unauthorized())
}

class DefaultUnauthorizedHandler extends UnauthorizedHandler

class MyUnauthorizedHandler @Inject() (implicit
  val messagesApi: MessagesApi,
  webJarAssets: WebJarAssets
) extends UnauthorizedHandler with I18nSupport {
  override val onUnauthorized: RequestHeader => Result =
    request => {
      import forms.Forms
      import views.html.login
      implicit val req = request
      Unauthorized(login(Forms.loginForm.withError("alert", "Invalid login credentials. Please try logging in again.")))
    }
}

class Authentication @Inject() (
  unauthorizedHandler: UnauthorizedHandler,
  users: Users
)(implicit
  val messagesApi: MessagesApi,
  webJarAssets: WebJarAssets
) extends UnauthorizedHandler with I18nSupport {

  def parseUserFromCookie(implicit request: RequestHeader): Option[User] = {
    request.session.get("userId").flatMap(users.findByUserId)
  }

  def parseUserFromQueryString(implicit request: RequestHeader): Option[User] = {
    val query = request.queryString.map { case (k, v) => k -> v.mkString }
    val userId = query.get("userId")
    val password = query.get("password")
    (userId, password) match {
      case (Some(e), Some(p)) => users.findByUserId(e).filter(user => user.passwordMatches(p))
      case _ => None
    }
  }

  def parseUserFromRequest(implicit request: RequestHeader): Option[User] =
    parseUserFromQueryString orElse parseUserFromCookie

  object SecuredAction extends AuthenticatedBuilder[User](
    userinfo = req => parseUserFromRequest(req),
    onUnauthorized = unauthorizedHandler.onUnauthorized
  )

  def UserAwareAction(action: RequestHeader => Result): AuthenticatedBuilder[User] = {
    new AuthenticatedBuilder[User](req => parseUserFromRequest(req), action)
  }
}
