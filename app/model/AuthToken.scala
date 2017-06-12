package model

import org.joda.time.DateTime
import scala.util.Random

object AuthToken extends IdImplicitLike {
  import org.joda.time.format.DateTimeFormat
  private lazy val fmt = DateTimeFormat.forPattern("yyyy-MM-dd '@' HH:mm:ss")

  private val random = Random
  def nextId: Id = random.nextLong.toId
}

/** A token to authenticate a user against an endpoint for a short time period.
 * @param id The unique token ID.
 * @param userID The unique ID of the user the token is associated with.
 * @param expiry The date-time the token expires. */
case class AuthToken(
  userID: Id,
  expiry: DateTime,
  override val id: Option[Id] = Some(AuthToken.nextId)
) extends HasId {
  override def toString = s"AuthToken: userID=$userID, expiry: ${ AuthToken.fmt.print(expiry) }, id='${ id.mkString }'"
}
