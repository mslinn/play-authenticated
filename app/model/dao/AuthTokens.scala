package model.dao

import model.{AuthToken, Id}
import org.joda.time.{DateTime, DateTimeZone}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

object AuthTokens extends QuillImplicits {
  import model.dao.QuillImplicits.ctx._

  /** Creates a new auth token and saves it in the backing store.
    * @param uid The user ID for which the token should be created.
    * @return The saved auth token. */
  def create(uid: Id, expiry: DateTime): (String, String, Option[AuthToken]) = {
    if (findByUid(uid).isDefined) {
      ("error", s"An AuthToken has already been assigned to you; TODO need to improve this error message.", None)
    } else {
      run { quote { query[AuthToken].insert(lift(AuthToken(uid=uid, expiry=expiry))) } }
      // Quill forces autoincrement if .returning is invoked
      val authToken: Option[AuthToken] = AuthTokens.findByUid(uid)
      println(s"authToken=$authToken")
      play.api.Logger.logger.debug(s"authToken=$authToken")
      ("success", s"Created AuthToken for user $uid.", authToken)
    }
  }

  def delete(authToken: AuthToken): Unit = {
    run { queryById(authToken.id).delete }
    ()
  }

  def findAll: Vector[AuthToken] = run { quote { query[AuthToken] } }.toVector

  def findById(id: Id): Option[AuthToken] = run { queryById(id) }.headOption

  /** @return token for user with Id uid */
  def findByUid(uid: Id): Option[AuthToken] =
    run { query[AuthToken].filter(_.uid == lift(uid)) }.headOption

  def findExpired(dateTime: DateTime): Vector[AuthToken] = findAll.filter { _.expiry.isBefore(dateTime) }

  def save(authToken: AuthToken): AuthToken = {
    run { quote { query[AuthToken].insert(lift(authToken)) } }
    authToken
  }

  def update(authToken: AuthToken): Long = run { queryById(authToken.id).update(lift(authToken)) }

  protected def queryById(id: Id): Quoted[EntityQuery[AuthToken]] =
    quote { query[AuthToken].filter(_.id == lift(id)) }

  /** Cleans expired tokens. */
  def deleteExpiredTokens()
                         (implicit ec: ExecutionContext): Unit = Future {
    for {
      token <- findExpired(DateTime.now.withZone(DateTimeZone.UTC))
    } delete(token)
  }
}
