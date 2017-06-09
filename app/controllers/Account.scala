package controllers

import javax.inject.Inject
import forms.Forms._
import models.dao.Users
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

class Account @Inject()(
  authenticated: Authenticated,
  val messagesApi: MessagesApi,
  users: Users
) extends Controller with I18nSupport {
  def myAccount = authenticated.Authenticated { implicit request =>
    Ok(views.html.myAccount(request.user))
  }

  def signup = Action {
    Ok(views.html.signup(signupForm))
  }

  def saveUser = Action { implicit request =>
    signupForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.signup(formWithErrors)),
      userData => {
        users.create(userData.email, userData.password)
        Redirect(routes.Account.myAccount()).withSession(("email", userData.email))
      }
    )
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def performLogin = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.login(formWithErrors)),
      userData => {
        val user  = users.findByEmail(userData.email).filter(user => user.checkPassword(userData.password))
        user.map(u => Redirect(routes.Account.myAccount()).withSession(("uuid", u.uuid))).getOrElse(
          Unauthorized(views.html.login(loginForm)).flashing("alert" -> "Bad credentials"))
      }
    )
  }

  def logout = Action { implicit request =>
    Redirect(routes.Account.login()).withNewSession.flashing("alert" -> "You've been logged out")
  }
}
