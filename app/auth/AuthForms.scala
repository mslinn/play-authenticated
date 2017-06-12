package auth

import model.{Email, Password, UserId}
import play.api.data.Form
import play.api.data.Forms._

case class ChangePasswordData(newPassword: Password)

case class LoginData(userId: UserId, password: Password)

object SignUpData {
  def encrypt(email: Email, userId: UserId, password: Password) =
    SignUpData(email=email, userId=userId, password=PasswordHasher.hash(password.value))
}

case class SignUpData(email: Email, userId: UserId, password: Password)

object AuthForms extends FormFormatterLike {
  val changePasswordForm: Form[ChangePasswordData] = Form(mapping(
      "new-password" -> passwordMapping
    )(ChangePasswordData.apply)(ChangePasswordData.unapply)
  )

  val loginForm: Form[LoginData] = Form(
    mapping(
      "userId"   -> userId,
      "password" -> passwordMapping
    )(LoginData.apply)(LoginData.unapply)
  )

  val signUpForm: Form[SignUpData] = Form(
    mapping(
      "email"    -> eMail,
      "userId"   -> userId,
      "password" -> passwordMapping
    )(SignUpData.encrypt)(SignUpData.unapply)
  )
}
