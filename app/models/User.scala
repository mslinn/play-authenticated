package models

import java.util.UUID

import anorm.SqlParser._
import anorm._
import play.api.Logger
import play.api.Play.current
import play.api.db.DB


case class User(
    id: Option[Long],
    uuid: String,
    email: Option[String],
    password: Option[String]) {

  def checkPassword(password: String): Boolean = this.password.contains(password)
}

object User {

  // Parsers

  val simple: RowParser[User] = {
    get[Option[Long]]("user.id") ~
    str("user.uuid") ~
    get[Option[String]]("user.email") ~
    get[Option[String]]("user.password") map {
      case id~uuid~email~password =>
        User(id, uuid, email, password)
    }
  }

  // Queries

  def findByEmail(email: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user where email = {email}").on('email -> email).as(simple.singleOpt)
    }
  }

  def create(email: String, password: String): String = {
    val uuid = UUID.randomUUID().toString
    DB.withConnection { implicit connection =>
      SQL(
        """
          |insert into user (uuid, email, password)
          |values ({uuid}, {email}, {password})
        """.stripMargin)
        .on('uuid -> uuid, 'email -> email, 'password -> password)
        .executeInsert()
    }
    Logger.info(s"Created user: $uuid")
    uuid
  }
}
