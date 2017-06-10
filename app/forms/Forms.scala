package forms

import models._
import play.api.data.Form
import play.api.data.Forms._

object Forms {
  val signupForm = Form(
    mapping(
      "email"    -> email,
      "userId"   -> nonEmptyText,
      "password" -> nonEmptyText
    )(SignupData.apply)(SignupData.unapply)
  )

  val loginForm = Form(
    mapping(
      "userId"   -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginData.apply)(LoginData.unapply)
  )
}
