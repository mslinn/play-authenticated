package controllers

import javax.inject.Inject
import forms.Forms._
import models.Authenticated
import models.dao.Users
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

class Account @Inject()(
  authenticated: Authenticated,
  users: Users
)(implicit
  val messagesApi: MessagesApi,
  webJarAssets: WebJarAssets
) extends Controller with I18nSupport {
  import authenticated._

  def myAccount = SecuredAction { implicit request =>
    Ok(views.html.myAccount(request.user))
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
            Redirect(routes.Account.myAccount())
              .withSession(("email", userData.email))

          case (k, v) =>
            Redirect(routes.Account.login())
              .flashing(k -> v)
        }
      }
    )
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def performLogin = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.login(formWithErrors)),
      userData => {
        val user  = users.findByUserId(userData.userId).filter(user => user.passwordMatches(userData.password))
        user.map(u => Redirect(routes.Account.myAccount()).withSession(("userId", u.userId)))
          .getOrElse(Unauthorized(views.html.login(loginForm.withError("alert", "Bad credentials"))))
      }
    )
  }

  def logout = Action { implicit request =>
    Redirect(routes.Account.login()).withNewSession.flashing("alert" -> "You've been logged out")
  }
}
