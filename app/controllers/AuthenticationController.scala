package controllers

import javax.inject.Inject
import authentication.{Authentication, UnauthorizedHandler}
import forms.Forms._
import models.dao.Users
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{Action, Controller, RequestHeader, Result}

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

class AuthenticationController @Inject()(
  authentication: Authentication,
  unauthorizedHandler: UnauthorizedHandler,
  users: Users
)(implicit
  val messagesApi: MessagesApi,
  webJarAssets: WebJarAssets
) extends Controller with I18nSupport {
  import authentication._

  def showAccountDetails = SecuredAction { implicit request =>
    Ok(views.html.showAccountDetails(request.user))
  }

  def signup = Action { implicit request =>
    Ok(views.html.signup(signupForm))
  }

  def saveUser = Action { implicit request =>
    signupForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.signup(formWithErrors)),
      userData => {
        users.create(userData.email, userData.userId, userData.password) match {
          case (k, _) if k=="success" =>
            Redirect(routes.AuthenticationController.showAccountDetails())
              .withSession(("userId", userData.userId))

          case (k, v) =>
            Redirect(routes.AuthenticationController.login())
              .flashing(k -> v)
        }
      }
    )
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def performLogin = Action { implicit request =>
    import views.html.{login => loginView}
    loginForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(loginView(formWithErrors)),
      userData => {
        val user = users.findByUserId(userData.userId).filter(_.passwordMatches(userData.password))
        user
          .map { u => Redirect(routes.AuthenticationController.showAccountDetails()).withSession(("userId", u.userId)) }
          .getOrElse {
            unauthorizedHandler.onUnauthorized(request)
          }
      }
    )
  }

  def logout = Action { implicit request =>
    Redirect(routes.AuthenticationController.login())
      .withNewSession
      .flashing("alert" -> "You've been logged out. Log in again below:")
  }
}
