package auth

import javax.inject.Inject
import akka.stream.Materializer
import model.dao.Users
import model.{ClearTextPassword, User, UserId}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Unauthorized
import play.api.mvc.Security.AuthenticatedBuilder
import play.api.mvc.{Action, AnyContent, BodyParser, BodyParsers, PlayBodyParsers, Request, RequestHeader, Result, WrappedRequest}
import scala.concurrent.ExecutionContext

trait UnauthorizedHandler {
  /** Default value is the standard Play unauthorized page */
  def onUnauthorized: RequestHeader => Result =
    _ => Unauthorized(views.html.defaultpages.unauthorized())
}

class DefaultUnauthorizedHandler extends UnauthorizedHandler

object Authentication {
  def parseUserFromCookie(implicit users: Users, request: RequestHeader): Option[User] =
    request.session
      .get("userId")
      .flatMap(id => users.findByUserId(UserId(id)))

  def parseUserFromQueryString(implicit users: Users, request: RequestHeader): Option[User] = {
    val query: Map[String, String] = request.queryString.map { case (k, v) => k -> v.mkString }
    val userId: Option[UserId] = query.get("userId").map(UserId.apply)
    val password: Option[ClearTextPassword] = query.get("clearTextPassword").map(ClearTextPassword.apply)
    (userId, password) match {
      case (Some(u), Some(p)) => users.findByUserId(u).filter(_.passwordMatches(p))
      case _ => None
    }
  }

  def parseUserFromRequest(implicit users: Users, request: RequestHeader): Option[User] =
    (parseUserFromQueryString orElse parseUserFromCookie).filter(_.activated)
}

class Authentication @Inject() (
  playBodyParsers: PlayBodyParsers,
  unauthorizedHandler: UnauthorizedHandler,
  users: Users
)(implicit
  val messagesApi: MessagesApi,
  ec: ExecutionContext,
  mat: Materializer
) extends UnauthorizedHandler with I18nSupport {
  object SecuredAction extends AuthenticatedBuilder[User](
    userinfo = req => Authentication.parseUserFromRequest(users, req),
    defaultParser = new BodyParsers.Default,
    onUnauthorized = unauthorizedHandler.onUnauthorized
  )

  /** An action that adds the current activated user in the request if available */
  def UserAwareAction[A](p: BodyParser[A])(f: RequestWithUser[A] => Result): Action[A] = Action(p) { implicit request =>
    val maybeUser: Option[User] = Authentication.parseUserFromRequest(users, request)
    f(RequestWithUser(maybeUser, request))
  }

  /** An action that adds the current user in the request if its available */
  def UserAwareAction(f: RequestWithUser[AnyContent] => Result): Action[AnyContent] =
    UserAwareAction(playBodyParsers.default)(f)
}

/** A request that adds the User for the current call */
case class RequestWithUser[A](user: Option[User], request: Request[A]) extends WrappedRequest(request)
