package models.dao

import javax.inject.Inject
import io.getquill.H2JdbcContext
import models.User
import scala.language.postfixOps

class Users @Inject() {
  lazy val ctx = new H2JdbcContext[TableNameSnakeCase]("quill")
  import ctx._

  def findByUserId(userId: String): Option[User] =
    ctx.run(query[User].filter(_.userId == lift(userId))).headOption

  def create(email: String, userId: String, password: String): (String, String) = {
    if (findByUserId(userId).isDefined) {
      "error" -> s"UserID $userId is already in use."
    } else {
      val q = quote {
        query[User].insert(lift(User(email=email, userId=userId, password=password))).returning(_.id)
      }
      val returnedIds: Option[Long] = ctx.run(q)
      "success" -> s"Created user $userId"
    }
  }
}
