package controllers.authentication

import java.net.URL
import javax.inject.Inject
import auth.AuthForms._
import auth.{AuthForms, Authentication, PasswordHasher, SignUpData, UnauthorizedHandler}
import com.micronautics.Smtp
import controllers.WebJarAssets
import controllers.authentication.routes.{AuthenticationController => AuthRoutes}
import model.dao.{AuthTokens, Users}
import model.{AuthToken, EMail, Id, User, UserId}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{Action, Controller, RequestHeader, Result}
import play.twirl.api.Html
import views.html.htmlForm.CSRFHelper
import views.html.{changePassword, forgotPassword, resetPassword}
import scala.concurrent.{ExecutionContext, Future}

object AuthenticationController {
  protected def sendEmail(toUser: User, subject: String)(bodyFragment: String)
                         (implicit messages: Messages, smtp: Smtp): Unit = {
    val completeBody = s"""<html>
                          |  <body>
                          |    <p>Dear ${ toUser.fullName },</p>
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
      subject = subject
    )(body = completeBody)
  }

  def sendActivateAccountEmail(toUser: User, url: URL)
                              (implicit messages: Messages, smtp: Smtp): Unit =
    sendEmail(toUser=toUser, subject=messages("email.activate.account.subject", url.getHost, AuthToken.expires)) {
      val message = messages("email.activate.account.html.text", url)
      s"<p>$message</p>\n"
    }

  def sendAlreadySignUpEMail(toUser: User, url: URL)
                            (implicit messages: Messages, smtp: Smtp): Unit =
    sendEmail(toUser=toUser, subject=messages("email.already.signed.up.subject")) {
      s"<p>${ messages("email.already.signed.up.html.text", url) }</p>\n"
    }

  def sendResetPasswordEMail(toUser: User, url: URL)
                            (implicit messages: Messages, smtp: Smtp): Unit =
     sendEmail(toUser=toUser, subject=messages("email.reset.password.subject")) {
       s"<p>${ messages("email.reset.password.html.text", url) }</p>\n"
     }

  def sendSignUpEMail(toUser: User, url: URL)
                     (implicit messages: Messages, smtp: Smtp): Unit =
     sendEmail(toUser=toUser, subject=messages("email.sign.up.subject")) {
       s"<p>${ messages("email.sign.up.html.text", url, AuthToken.expires) }</p>\n"
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
   * @param tokenId The token that identifies a user. */
  def activateUser(tokenId: Id) = Action.async { implicit request =>
    Future {
      val result = for {
        token <- AuthTokens.findById(tokenId)
        user  <- users.findById(Some(token.uid))
      } yield {
        users.update(user.copy(activated=true))
        AuthTokens.delete(token)
        Redirect(AuthRoutes.showLoginView())
          .flashing("success" -> Messages("account.activated"))
      }
      result.getOrElse(
        Redirect(AuthRoutes.showSignUpView())
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

  def showSignUpView = Action { implicit request =>
    val form: Form[SignUpData] = request.session
      .get("error")
      .map(error => signUpForm.withError("error", error))
      .getOrElse(signUpForm)
    Ok(views.html.signup(form))
  }

  def saveNewUser = Action { implicit request =>
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
          case (k, v) if k=="success" =>
            Redirect(AuthRoutes.sendAccountActivationEmail(userData.userId))
              .withSession("userId" -> userData.userId.value)

          case (k, v) =>
            Redirect(AuthRoutes.showSignUpView())
              .withSession(k -> v)
        }
      }
    )
  }

  def showLoginView = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def performLogin = Action { implicit request =>
    import views.html.{login => loginView}
    loginForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(loginView(formWithErrors)),
      loginData => {
        users
          .findByUserId(loginData.userId)
          .filter(_.passwordMatches(loginData.password))
          .map {
            case user if user.activated =>
              Redirect(AuthRoutes.showAccountDetails())
                .withSession("userId" -> user.userId.value)

            case user =>
              val formWithError = AuthForms.loginForm.withError("error",
                s"""You have not yet activated this account.
                   |Please find the email sent to ${ user.email } from ${ smtp.smtpFrom },
                   |and click on the link in the email so this account will be activated.""".stripMargin)
              Unauthorized(views.html.login(formWithError))
          }.getOrElse {
            unauthorizedHandler.onUnauthorized(request)
          }
      }
    )
  }

  def logout = Action { implicit request =>
    Redirect(routes.AuthenticationController.showLoginView())
      .withNewSession
      .flashing("warning" -> "You've been logged out. Log in again below:")
  }

  /** Sends an account activation email to the user with the given userId.
   * @param userId The userId of the user to send the activation mail to.
   * @return The result to display. */
  def sendAccountActivationEmail(userId: UserId) = Action.async { implicit request =>
    def successResult(email: EMail, key: String, value: String) =
      Redirect(AuthRoutes.awaitConfirmation())
        .flashing(key -> value)

    def errorResult(userId: UserId) =
      Redirect(AuthRoutes.showSignUpView())
        .flashing("error" -> s"No User found with ID $userId")

    val result: Future[Result] = Future.successful {
      users.findByUserId(userId) match {
        case Some(user) =>
          user.id.map { id =>
            val (key, value, maybeAuthToken) = AuthTokens.create(uid=id)
            maybeAuthToken.map { authToken =>
              val urlStr = AuthRoutes.activateUser(authToken.id).absoluteURL()
              AuthenticationController.sendActivateAccountEmail(toUser = user, url = new java.net.URL(urlStr))
              successResult(user.email, "success", Messages("activation.email.sent", user.email.value, smtp.smtpFrom))
            } getOrElse {
              successResult(user.email, key, value)
            }
          }.getOrElse(errorResult(userId))

        case None =>
          errorResult(userId)
      }
    }
    result
  }

  /** Displays the `Forgot Password` page. */
  def showForgetPasswordView = Action.async { implicit request =>
    Future.successful(Ok(forgotPassword(AuthForms.forgotPasswordForm)))
  }

  /** Sends an email with password reset instructions to the given address if it exists in the database.
    * If any failure, enforce security by not showing the user any existing `userIds`. */
  def submitForgetPassword = Action.async { implicit request =>
    Future {
      AuthForms.forgotPasswordForm.bindFromRequest.fold(
        formWithErrors => BadRequest(forgotPassword(formWithErrors)),
        forgotPasswordData => {
          users.findByUserId(forgotPasswordData.userId) match {
            case Some(user) =>
              val (key, value, maybeAuthToken) = AuthTokens.create(user.id.get)
              maybeAuthToken.map { authToken =>
                AuthTokens.delete(authToken)
                val url: String = AuthRoutes.showResetPasswordView(authToken.id).absoluteURL
                EMail.send(
                  to = user.email,
                  subject = Messages("email.reset.password.subject")
                ) {
                  s"""<html>
                     |<body>
                     |  <p>${ messagesApi("email.reset.password.hello", user.fullName) }</p>
                     |  <p>${ Html(messagesApi("email.reset.password.html.text", url)) }</p>
                     |</body>
                     |</html>
                     |""".stripMargin
                }
                Redirect(AuthRoutes.showLoginView())
                  .flashing("success" -> Messages("reset.email.sent"))
              }.getOrElse {
                Redirect(AuthRoutes.showLoginView())
                  .flashing(key -> value)
              }

            case None =>
              Redirect(AuthRoutes.showLoginView())
                .flashing("error" -> "No user is associated with that userId")
          }
        }
      )
    }
  }

  /** Displays the `Reset Password` page.
   * @param tokenId The token id that identifies a user. */
  def showResetPasswordView(tokenId: Id) = Action.async { implicit request =>
    Future {
      AuthTokens.findById(tokenId) match {
        case Some(_) =>
          Ok(resetPassword(AuthForms.resetPasswordForm, tokenId))

        case None =>
          Redirect(AuthRoutes.showLoginView())
            .flashing("error" -> Messages("invalid.reset.link"))
      }
    }
  }

  /** Resets the password.
   * @param tokenId The id of the token that identifies a user. */
  def submitResetPassword(tokenId: Id) = Action.async { implicit request =>
    Future {
      AuthTokens.findById(tokenId) match {
        case Some(authToken) =>
          AuthTokens.delete(authToken)
          AuthForms.resetPasswordForm.bindFromRequest.fold(
            formWithErrors => { BadRequest(resetPassword(formWithErrors, tokenId)) },
            changePasswordData => {
                users.findById(Some(authToken.uid)) match {
                case Some(user) =>
                  users.update(user.copy(password=PasswordHasher.hash(changePasswordData.newPassword)))
                  Redirect(AuthRoutes.showLoginView())
                    .flashing("success" -> Messages("password.reset"))

                case None =>
                  Redirect(AuthRoutes.showLoginView())
                    .flashing("error" -> Messages("invalid.reset.link"))
              }
            }
          )

        case None =>
          Redirect(AuthRoutes.showLoginView())
            .flashing("error" -> Messages("invalid.reset.link"))
      }
    }
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
