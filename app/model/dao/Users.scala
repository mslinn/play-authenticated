package model.dao

import io.getquill.H2JdbcContext
import model.{Email, Id, Password, User, UserId}
import scala.language.postfixOps

class Users {
  lazy val ctx = new H2JdbcContext[TableNameSnakeCase]("quill")
  import ctx._

  def findAll: Vector[User] = run { quote { query[User] } }.toVector

  def findById(id: Option[Id]): Option[User] = run { queryById(id) }.headOption

  def findByUserId(userId: UserId): Option[User] =
    run { query[User].filter(_.userId == lift(userId)) }.headOption

  def create(email: Email, userId: UserId, password: Password): (String, String) = {
    if (findByUserId(userId).isDefined) {
      "error" -> s"UserID $userId is already in use."
    } else {
      run { quote {
        query[User]
          .insert(lift(User(email=email, userId=userId, password=password)))
          .returning(_.id.map(_.value))
      } }
      "success" -> s"Created user $userId"
    }
  }

  def update(user: User): Long = run { queryById(user.id).update(lift(user)) }

  def delete(user: User): Unit = {
    run { queryById(user.id).delete }
    ()
  }

  protected def queryById(id: Option[Id]): Quoted[EntityQuery[User]] =
    quote { query[User].filter(_.id == lift(id)) }
}
