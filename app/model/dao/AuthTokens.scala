package model.dao

import io.getquill.H2JdbcContext
import model.{AuthToken, Id}
import org.joda.time.{DateTime, DateTimeZone}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

object AuthTokens extends Implicits {
  lazy val ctx: H2JdbcContext[TableNameSnakeCase] = new H2JdbcContext[TableNameSnakeCase]("quill")
  import ctx._

  /** Creates a new auth token and saves it in the backing store.
     * @param uid The user ID for which the token should be created.
     * @param expiry The duration a token expires.
     * @return The saved auth token. */
  // todo make the timeout a config param
  def create(uid: Id, id: Id=AuthToken.nextId, expiry: DateTime=DateTime.now.plusHours(3)): (String, String, Option[AuthToken]) = {
    if (findByUid(uid).isEmpty) {
      ("error", s"No token for user; need to improve this error message.", None)
    } else {
      val id2: Id = run { quote {
        query[AuthToken]
          .insert(lift(AuthToken(uid=uid, expiry=expiry, id=id)))
          .returning(_.id)
      } }
      val authToken = AuthTokens.findById(id2)
      ("success", s"Created AuthToken for user $uid; need to improve this error message.", authToken)
    }
  }

  /** Creates a new auth token and saves it in the backing store.
   * @param userID The user ID for which the token should be created.
   * @param expiry The duration a token expires.
   * @return The saved auth token. */
  // todo make the timeout a config param
 /* def create(userID: Id, expiry: FiniteDuration = 5 hours)
            (implicit ec: ExecutionContext): Future[AuthToken] = Future {
    val token = AuthToken(userID, DateTime.now.withZone(DateTimeZone.UTC).plusSeconds(expiry.toSeconds.toInt))
    save(token)
  }*/

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
    val id: Id = run { quote {
      query[AuthToken]
        .insert(lift(authToken))
        .returning(_.id)
    } }
    authToken.copy(id=id)
  }

  def update(authToken: AuthToken): Long = run { queryById(authToken.id).update(lift(authToken)) }

  protected def queryById(id: Id): Quoted[EntityQuery[AuthToken]] =
    quote { query[AuthToken].filter(_.id == lift(id)) }

  /** Validates a token ID.
   * @param id The token ID to validate.
   * @return The token if it's valid, None otherwise. */
  def validate(id: Id): Option[AuthToken] = findById(id)

  /** Cleans expired tokens. */
  def deleteExpiredTokens()
                         (implicit ec: ExecutionContext): Unit = Future {
    for {
      token <- findExpired(DateTime.now.withZone(DateTimeZone.UTC))
    } delete(token)
  }
}
