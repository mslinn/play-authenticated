package models.dao

import javax.inject.Inject
import io.getquill.{Escape, H2JdbcContext, NamingStrategy, SnakeCase}
import models._
import play.api.db.Database
import scala.language.postfixOps

trait ModifiedSnakeCase extends NamingStrategy with Escape with SnakeCase {
  override def table(s: String): String   = {
    val x = super.default(s)
    val y = if (x.startsWith("_")) x.substring(1) else x
    s""""$y""""
  }

  override def column(s: String): String  = {
    val x = super.default(s)
    if (x.startsWith("_")) x.substring(1) else x
  }

  override def default(s: String): String = super.default(s)
}

object ModifiedSnakeCase extends ModifiedSnakeCase

class Users @Inject() (db: Database) {
  lazy val ctx = new H2JdbcContext[ModifiedSnakeCase]("ctx")
  import ctx._

  def findByUserId(userId: String): Option[User] = {
    val m: Option[User] = ctx.run(query[User].filter(_.userId == lift(userId))).headOption
    m
  }

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
