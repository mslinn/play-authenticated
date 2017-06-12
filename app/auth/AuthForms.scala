package auth

import play.api.data.Form
import play.api.data.Forms._

case class ChangePasswordData(newPassword: String)

case class LoginData(userId: String, password: String)

object SignUpData {
  def encrypt(email: String, userId: String, password: String) =
    SignUpData(email=email, userId=userId, password=PasswordHasher.hash(password))
}

case class SignUpData(email: String, userId: String, password: String)

object AuthForms {
  val changePasswordForm: Form[ChangePasswordData] = Form(mapping(
      "new-password"     -> nonEmptyText
    )(ChangePasswordData.apply)(ChangePasswordData.unapply)
  )

  val loginForm: Form[LoginData] = Form(
    mapping(
      "userId"   -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginData.apply)(LoginData.unapply)
  )

  val signUpForm: Form[SignUpData] = Form(
    mapping(
      "email"    -> email,
      "userId"   -> nonEmptyText,
      "password" -> nonEmptyText
    )(SignUpData.encrypt)(SignUpData.unapply)
  )
}
