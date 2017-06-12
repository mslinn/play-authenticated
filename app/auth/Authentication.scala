package auth

import controllers.WebJarAssets
import javax.inject.Inject
import model.User
import model.dao.Users
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, BodyParser, Request, RequestHeader, Result, WrappedRequest}
import play.api.mvc.Results.Unauthorized
import play.api.mvc.Security.AuthenticatedBuilder
import scala.concurrent.Future

trait UnauthorizedHandler {
  /** Default value is the standard Play unauthorized page */
  def onUnauthorized: RequestHeader => Result =
    _ => Unauthorized(views.html.defaultpages.unauthorized())
}

class DefaultUnauthorizedHandler extends UnauthorizedHandler

object Authentication {
  def parseUserFromCookie(implicit users: Users, request: RequestHeader): Option[User] =
    request.session.get("userId").flatMap(users.findByUserId)

  def parseUserFromQueryString(implicit users: Users, request: RequestHeader): Option[User] = {
    val query = request.queryString.map { case (k, v) => k -> v.mkString }
    val userId = query.get("userId")
    val password = query.get("password")
    (userId, password) match {
      case (Some(e), Some(p)) => users.findByUserId(e).filter(user => user.passwordMatches(p))
      case _ => None
    }
  }

  def parseUserFromRequest(implicit users: Users, request: RequestHeader): Option[User] =
    parseUserFromQueryString orElse parseUserFromCookie
}

class Authentication @Inject() (
  unauthorizedHandler: UnauthorizedHandler,
  users: Users
)(implicit
  val messagesApi: MessagesApi,
  webJarAssets: WebJarAssets
) extends UnauthorizedHandler with I18nSupport {
  object SecuredAction extends AuthenticatedBuilder[User](
    userinfo = req => Authentication.parseUserFromRequest(users, req),
    onUnauthorized = unauthorizedHandler.onUnauthorized
  )

  // todo not sure how to create an instance of this class, or if it even necessary
  class UserAwareAction(action: Request[AnyContent] => Future[Result]) extends Action[AnyContent] {
    override def parser: BodyParser[AnyContent] = ???

    override def apply(request: Request[AnyContent]): Future[Result] = action(request)
  }
}

// todo not sure how to create an instance of this class, or if it even necessary
case class UserAwareRequest(maybeUser: Option[User], request: RequestHeader)
  extends WrappedRequest(request.asInstanceOf[Request[AnyContent]])
