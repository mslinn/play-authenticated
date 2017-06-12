package model

import auth.PasswordHasher

case class Email(value: String) extends AnyVal
case class Id(value: Long) extends AnyVal
case class Password(value: String) extends AnyVal
case class UserId(value: String) extends AnyVal

case class User(
  userId: UserId,
  email: Email,
  password: Password,
  id: Option[Id]=None
) {
  def passwordMatches(clearText: Password): Boolean = this.password == PasswordHasher.hash(clearText.value)
}
