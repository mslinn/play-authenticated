package auth

import model.{ClearTextPassword, EMail, UserId}
import play.api.data.Form
import play.api.data.Forms._

case class ChangePasswordData(newPassword: ClearTextPassword)

case class ForgotPasswordData(userId: UserId)

case class LoginData(userId: UserId, password: ClearTextPassword)

case class SignUpData(
  email: EMail,
  userId: UserId,
  clearTextPassword: ClearTextPassword,
  firstName: String,
  lastName: String
)

object AuthForms extends FormFormatterLike {
  val changePasswordForm: Form[ChangePasswordData] = Form(mapping(
      "new-password" -> clearTextPasswordMapping
    )(ChangePasswordData.apply)(ChangePasswordData.unapply)
  )

  val forgotPasswordForm: Form[ForgotPasswordData] = Form(mapping(
      "userId" -> userId
    )(ForgotPasswordData.apply)(ForgotPasswordData.unapply)
  )

  val loginForm: Form[LoginData] = Form(
    mapping(
      "userId"   -> userId,
      "password" -> clearTextPasswordMapping
    )(LoginData.apply)(LoginData.unapply)
  )

  val resetPasswordForm: Form[ChangePasswordData] = Form(mapping(
      "password" -> clearTextPasswordMapping
    )(ChangePasswordData.apply)(ChangePasswordData.unapply)
  )

  val signUpForm: Form[SignUpData] = Form(
    mapping(
      "email"     -> eMail,
      "userId"    -> userId,
      "password"  -> clearTextPasswordMapping,
      "firstName" -> nonEmptyText,
      "lastName"  -> nonEmptyText
    )(SignUpData.apply)(SignUpData.unapply)
  )
}
