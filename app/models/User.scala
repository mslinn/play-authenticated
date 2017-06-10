package models

// TODO hash password
case class User(
  id: Option[Long],
  userId: String,
  email: String,
  password: String
) {
  def passwordMatches(password: String): Boolean = this.password == password
}
