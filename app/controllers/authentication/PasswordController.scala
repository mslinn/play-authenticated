package controllers.authentication

import javax.inject.Inject
import auth.AuthForms._
import auth.{Authentication, PasswordHasher}
import controllers.WebJarAssets
import controllers.authentication.routes.{PasswordController => PasswordRoutes}
import model.dao.Users
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Controller, WrappedRequest}
import views.html.changePassword
import views.html.htmlForm.CSRFHelper
import scala.concurrent.ExecutionContext

class PasswordController @Inject()(
  authentication: Authentication,
  val messagesApi: MessagesApi
)(implicit
  csrfHelper: CSRFHelper,
  ec: ExecutionContext,
  users: Users,
  webJarAssets: WebJarAssets
) extends Controller with I18nSupport {
  import authentication._

  /** Displays the `Change Password` page. */
  def showChangePasswordView = SecuredAction { implicit request =>
    implicit val maybeUser = Some(request.user)
    Ok(changePassword(changePasswordForm, request.user))
  }

  /** Changes the password. */
  def submitNewPassword = SecuredAction { implicit request =>
    changePasswordForm.bindFromRequest.fold(
      formWithErrors => BadRequest(changePassword(formWithErrors, request.user)),
      changePasswordData => {
        val hashedPassword = PasswordHasher.hash(changePasswordData.newPassword.value)
        users.update(request.user.copy(password = hashedPassword))
        Redirect(PasswordRoutes.showChangePasswordView())
          .flashing("success" -> Messages("password.changed"))
      }
    )
  }
}
