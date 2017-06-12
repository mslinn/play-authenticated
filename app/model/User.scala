package model

import auth.PasswordHasher

case class Email(value: String) extends AnyVal {
  override def toString: String = value
}

case class Id(value: Long) extends AnyVal {
  override def toString: String = value.toString
}

case class EncryptedPassword(value: String) extends AnyVal {
  override def toString: String = value
}

case class ClearTextPassword(value: String) extends AnyVal {
  def encrypt: EncryptedPassword = PasswordHasher.hash(this)

  override def toString: String = value
}

case class UserId(value: String) extends AnyVal {
  override def toString: String = value
}

case class User(
  userId: UserId,
  email: Email,
  password: EncryptedPassword,
  id: Option[Id]=None
) {
  def passwordMatches(clearTextPassword: ClearTextPassword): Boolean = {
    val result = PasswordHasher.matches(clearTextPassword, password)
    result
  }
}
