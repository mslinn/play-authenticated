package model.dao

import model.{EMail, EncryptedPassword, Id, User, UserId}
import scala.language.postfixOps

class Users extends QuillImplicits {
  import QuillImplicits.ctx._

  def findAll: Vector[User] = run { quote { query[User] } }.toVector

  def findById(id: Option[Id]): Option[User] = run { queryById(id) }.headOption

  def findByUserId(userId: UserId): Option[User] =
    run { query[User].filter(_.userId == lift(userId)) }.headOption

  def create(email: EMail, userId: UserId, password: EncryptedPassword, firstName: String, lastName: String): (String, String) = {
    if (findByUserId(userId).isDefined) {
      "error" -> s"UserID $userId is already in use."
    } else {
      run { quote {
        query[User]
          .insert(lift(User(email=email, firstName=firstName, lastName=lastName, userId=userId, password=password)))
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
