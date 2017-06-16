package model

import auth.PasswordHasher
import model.persistence._

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

/** @param activated set when the user clicks on a link in an activation email */
case class User(
  userId: UserId,
  email: EMail,
  firstName: String,
  lastName: String,
  password: EncryptedPassword,
  activated: Boolean = false,
  override val id: Id[Option[Long]] = Id.empty
) extends HasId[Option[Long]] {
  lazy val fullName: String = s"$firstName $lastName"

  def passwordMatches(clearTextPassword: ClearTextPassword): Boolean = {
    val result = PasswordHasher.matches(clearTextPassword, password)
    result
  }
}
