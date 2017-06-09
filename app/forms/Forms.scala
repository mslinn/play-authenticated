package forms

import controllers.{LoginData, SignupData}
import play.api.data.Form
import play.api.data.Forms._

object Forms {
  val signupForm = Form(
    mapping(
      "email"    -> email,
      "password" -> nonEmptyText
    )(SignupData.apply)(SignupData.unapply)
  )

  val loginForm = Form(
    mapping(
      "email"    -> email,
      "password" -> nonEmptyText
    )(LoginData.apply)(LoginData.unapply)
  )
}
