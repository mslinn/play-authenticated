package models.dao

import java.util.UUID
import javax.inject.Inject
import anorm.SqlParser._
import anorm._
import models._
import play.api.Logger
import play.api.db.Database

class Users @Inject() (db: Database) {
  val simple: RowParser[User] = {
    get[Option[Long]]("user.id") ~
    str("user.uuid") ~
    get[Option[String]]("user.email") ~
    get[Option[String]]("user.password") map {
      case id~uuid~email~password =>
        User(id, uuid, email, password)
    }
  }

  def findByEmail(email: String): Option[User] = {
    db.withConnection { implicit connection =>
      SQL("select * from user where email = {email}").on('email -> email).as(simple.singleOpt)
    }
  }

  def create(email: String, password: String): String = {
    val uuid = UUID.randomUUID().toString
    db.withConnection { implicit connection =>
      SQL(
        """insert into user (uuid, email, password)
          |  values ({uuid}, {email}, {password})
          |""".stripMargin)
        .on('uuid -> uuid, 'email -> email, 'password -> password)
        .executeInsert()
    }
    Logger.info(s"Created user: $uuid")
    uuid
  }
}
