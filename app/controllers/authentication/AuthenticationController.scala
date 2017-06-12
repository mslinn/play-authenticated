package controllers.authentication

import auth.{Authentication, SignUpData, UnauthorizedHandler}
import auth.AuthForms._
import controllers.WebJarAssets
import controllers.authentication.routes.{AuthenticationController => AuthRoutes}
import javax.inject.Inject
import model.dao.Users
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{Action, Controller, RequestHeader, Result}

class AuthenticationController @Inject()(
  authentication: Authentication,
  unauthorizedHandler: UnauthorizedHandler
)(implicit
  val messagesApi: MessagesApi,
  users: Users,
  webJarAssets: WebJarAssets
) extends Controller with I18nSupport {
  import authentication._

  def showAccountDetails = SecuredAction { implicit request =>
    Ok(views.html.showAccountDetails(request.user))
  }

  def signUp = Action { implicit request =>
    val form: Form[SignUpData] = request.session
      .get("error")
      .map(error => signUpForm.withError("error", error))
      .getOrElse(signUpForm)
    Ok(views.html.signup(form))
  }

  def saveUser = Action { implicit request =>
    signUpForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.signup(formWithErrors)),
      userData => {
        users.create(userData.email, userData.userId, userData.clearTextPassword.encrypt) match {
          case (k, _) if k=="success" =>
            Redirect(AuthRoutes.showAccountDetails())
              .withSession("userId" -> userData.userId.value)

          case (k, v) =>
            Redirect(AuthRoutes.signUp())
              .withSession(k -> v)
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
      loginData => {
        val maybeUser = users.findByUserId(loginData.userId)
        val result: Result = maybeUser
          .filter(_.passwordMatches(loginData.password))
          .map { u => Redirect(AuthRoutes.showAccountDetails()).withSession(("userId", u.userId.value)) }
          .getOrElse {
            unauthorizedHandler.onUnauthorized(request)
          }
        result
      }
    )
  }

  def logout = Action { implicit request =>
    Redirect(routes.AuthenticationController.login())
      .withNewSession
      .flashing("warning" -> "You've been logged out. Log in again below:")
  }
}

class MyUnauthorizedHandler @Inject() (implicit
  val messagesApi: MessagesApi,
  webJarAssets: WebJarAssets
) extends UnauthorizedHandler with I18nSupport {
  override val onUnauthorized: RequestHeader => Result =
    request => {
      import auth.AuthForms
      import views.html.login
      implicit val req = request
      Unauthorized(login(AuthForms.loginForm.withError("error", "Invalid login credentials. Please try logging in again.")))
    }
}
