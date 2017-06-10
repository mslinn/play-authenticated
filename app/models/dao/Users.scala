package models.dao

import javax.inject.Inject
import anorm.SqlParser._
import anorm._
import models._
import play.api.db.Database
import scala.language.postfixOps

class Users @Inject() (db: Database) {
  val userRowParser: RowParser[User] = {
    val rowParser = get[Option[Long]]("user.id") ~
            str("user.user_id") ~
            str("user.email") ~
            str("user.password")
    rowParser.map {
      case id ~ userId ~ email ~ password =>
        User(id, userId, email, password)
    }
  }

  def findByUserId(userId: String): Option[User] = {
    db.withConnection { implicit connection =>
      val v = SQL"""select * from "user"""".as(userRowParser *)
      val w = SQL"""select * from "user" where user_id = $userId""".sql
      val y = SQL"""select * from "user" where user_id = $userId""".executeQuery()
      val x = SQL"""select * from "user" where user_id = $userId""".as(userRowParser *)
      x.headOption
    }
  }

  def create(email: String, userId: String, password: String): (String, String) = {
    db.withConnection { implicit connection =>
      val users: SqlQueryResult = SQL"""select from "user" where user_id = "$userId""".executeQuery()
      if (!users.resultSetOnFirstRow) {
        "error" -> s"UserID $userId is already in use."
      } else {
        SQL"""insert into "user" (email, userId, password) values ($email, $userId, $password)""".executeInsert()
        "success" -> s"Created user $userId"
      }
    }
  }
}
