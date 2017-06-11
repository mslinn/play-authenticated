package models.dao

import io.getquill.H2JdbcContext
import models._
import scala.language.postfixOps

class Users {
  lazy val ctx = new H2JdbcContext[TableNameSnakeCase]("quill")
  import ctx._

  protected def queryById(id: Option[Long]): Quoted[EntityQuery[User]] = quote { query[User].filter(_.id == lift(id)) }

  def findById(id: Option[Long]): Option[User] = run { queryById(id) }.headOption

  def findByUserId(userId: String): Option[User] = run { query[User].filter(_.userId == lift(userId)) }.headOption

  def create(email: String, userId: String, password: String): (String, String) = {
    if (findByUserId(userId).isDefined) {
      "error" -> s"UserID $userId is already in use."
    } else {
      val q = quote {
        query[User].insert(lift(User(email=email, userId=userId, password=password))).returning(_.id)
      }
      val returnedIds: Option[Long] = run(q)
      "success" -> s"Created user $userId"
    }
  }

  def update(user: User): Long = run { queryById(user.id).update(lift(user)) }

  def delete(user: User): Unit = {
    run { queryById(user.id).delete }
    ()
  }
}
