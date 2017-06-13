package controllers.authentication

import java.net.URL
import auth.{Authentication, PasswordHasher, SignUpData, UnauthorizedHandler}
import auth.AuthForms._
import controllers.WebJarAssets
import controllers.authentication.routes.{AuthenticationController => AuthRoutes}
import javax.inject.Inject
import com.micronautics.Smtp
import model.EMail.emailConfig
import model.{EMail, Id, User, UserId}
import model.dao.{AuthTokens, Users}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{Action, Controller, RequestHeader, Result}
import views.html.changePassword
import views.html.htmlForm.CSRFHelper
import scala.concurrent.{ExecutionContext, Future}

object AuthenticationController {
  protected def sendEmail(toUser: User, subject: String)(bodyFragment: String)
                         (implicit messages: Messages, smtp: Smtp): Unit = {
    val completeBody = s"""<html>
                          |  <body>
                          |    <p>Dear ${ toUser.fullName }},</p>
                          |    $bodyFragment
                          |    <p>Thank you,<br/>
                          |      ${ smtp.smtpFrom }</p>
                          |</body>
                          |</html
                          |""".stripMargin

    EMail.send(
      to = toUser.email,
      cc = Nil, // todo add cc to conf
      bcc = Nil,
      subject = Messages("email.activate.account.subject")
    )(body = completeBody)
  }

  def activateAccountEmail(toUser: User, url: URL)
                          (implicit messages: Messages, smtp: Smtp): Unit =
    sendEmail(toUser=toUser, subject=messages("email.activate.account.subject")) {
      s"""    <p>${ messages("email.activate.account.html.text", url) }</p>
         |""".stripMargin
    }

  def alreadySignUpEMail(toUser: User, url: URL)
                        (implicit messages: Messages, smtp: Smtp): Unit =
    sendEmail(toUser=toUser, subject=messages("email.already.signed.up.subject")) {
      s"""    <p>${ messages("email.already.signed.up.html.text", url) }</p>
         |""".stripMargin
    }

  def resetPasswordEMail(toUser: User, url: URL)
                        (implicit messages: Messages, smtp: Smtp): Unit =
     sendEmail(toUser=toUser, subject=messages("email.reset.password.subject")) {
       s"""    <p>${ messages("email.reset.password.html.text", url) }</p>
          |""".stripMargin
     }

  def signUpEMail(toUser: User, url: URL)
                 (implicit messages: Messages, smtp: Smtp): Unit =
     sendEmail(toUser=toUser, subject=messages("email.sign.up.subject")) {
       s"""    <p>${ messages("email.sign.up.html.text", url) }</p>
          |""".stripMargin
     }
}

class AuthenticationController @Inject()(
  authentication: Authentication,
  unauthorizedHandler: UnauthorizedHandler
)(implicit
  csrfHelper: CSRFHelper,
  ec: ExecutionContext,
  val messagesApi: MessagesApi,
  users: Users,
  webJarAssets: WebJarAssets
) extends Controller with I18nSupport {
  import authentication._
  implicit lazy val smtp: Smtp = EMail.smtp

  /** Activates a User account; triggered when a user clicks on a link in an activation email.
   * @param token The token to identify a user.
   * @return The result to display. */
  def activate(token: Id) = Action.async { implicit request =>
    Future {
      val result = for {
        validToken <- AuthTokens.validate(token)
        user <- users.findById(Some(validToken.uid))
      } yield {
        users.update(user.copy(activated=true))
        Redirect(AuthRoutes.login())
          .flashing("success" -> Messages("account.activated"))
      }
      result.getOrElse(
        Redirect(AuthRoutes.signUp())
          .flashing("error" -> Messages("invalid.activation.link"))
      )
    }
  }

  def awaitConfirmation = Action { implicit request =>
    Ok(views.html.awaitingConfirmation())
  }

  /** Not really part of the library, should be shuffled off somewhere */
  def showAccountDetails = SecuredAction { implicit request =>
    Ok(views.html.showAccountDetails(request.user))
  }

  /** Displays the `Change EncryptedPassword` page. */
  def showChangePasswordView = SecuredAction { implicit request =>
    Ok(changePassword(changePasswordForm, request.user))
  }

  /** Changes the password. */
  def submitNewPassword = SecuredAction { implicit request =>
    changePasswordForm.bindFromRequest.fold(
      formWithErrors => BadRequest(changePassword(formWithErrors, request.user)),
      changePasswordData => {
        val hashedPassword = PasswordHasher.hash(changePasswordData.newPassword)
        users.update(request.user.copy(password = hashedPassword))
        Redirect(AuthRoutes.showChangePasswordView())
          .flashing("success" -> Messages("password.changed"))
      }
    )
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
        users.create(
          email     = userData.email,
          userId    = userData.userId,
          password  = userData.clearTextPassword.encrypt,
          firstName = userData.firstName,
          lastName  = userData.lastName
        ) match {
          case (k, _) if k=="success" =>
            Redirect(AuthRoutes.awaitConfirmation())
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

  /** Sends an account activation email to the user with the given userId.
   * @param userId The userId of the user to send the activation mail to.
   * @return The result to display. */
  def send(userId: UserId) = Action.async { implicit request =>
    def successResult(email: EMail, key: String, value: String) =
      Redirect(AuthRoutes.login())
        .flashing(key -> value)

    val result: Future[Result] = Future.successful {
      users.findByUserId(userId) match {
        case Some(user) =>
          user.id.map { id =>
            val (key, value, maybeAuthToken) = AuthTokens.create(uid=id)
            maybeAuthToken.map { authToken =>
              AuthenticationController.activateAccountEmail(
                toUser = user,
                url = new java.net.URL(AuthRoutes.activate(authToken.id).absoluteURL)
              )
              successResult(user.email, key, value)
            } getOrElse {
              successResult(user.email, "success", Messages("activation.email.sent", user.email.value))
            }
          }.getOrElse(Redirect(AuthRoutes.signUp())
            .flashing("error" -> Messages("activation.email.not.sent", user.email)))

        case None =>
          Redirect(AuthRoutes.signUp())
            .flashing("error" -> Messages("no user with userID", userId))
      }
    }
    result
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
