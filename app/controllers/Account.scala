package controllers

import javax.inject.Inject

import models.User
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

class Account @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val signupForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(SignupData.apply)(SignupData.unapply)
  )

  val loginForm = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText
    )(LoginData.apply)(LoginData.unapply)
  )

  def myAccount = Authenticated { implicit request =>
    Ok(views.html.myAccount(request.user))
  }

  def signup = Action {
    Ok(views.html.signup(signupForm))
  }

  def saveUser = Action { implicit request =>
    signupForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.signup(formWithErrors)),
      userData => {
        val uuid = User.create(userData.email, userData.password)
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
        val user  = User.findByEmail(userData.email).filter(user => user.checkPassword(userData.password))
        user.map(u => Redirect(routes.Account.myAccount()).withSession(("uuid", u.uuid))).getOrElse(
          Unauthorized(views.html.login(loginForm)).flashing("alert" -> "Bad credentials"))
      }
    )
  }

  def logout = Action { implicit request =>
    Redirect(routes.Account.login()).withNewSession.flashing("alert" -> "You've been logged out")
  }
}
