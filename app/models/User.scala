package models

// TODO hash password
case class User(
  userId: String,
  email: String,
  password: String,
  id: Option[Long]=None
) {
  def passwordMatches(password: String): Boolean = this.password == password
}
