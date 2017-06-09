package models

case class User(
  id: Option[Long],
  uuid: String,
  email: Option[String],
  password: Option[String]
) {
  def checkPassword(password: String): Boolean = this.password.contains(password)
}
