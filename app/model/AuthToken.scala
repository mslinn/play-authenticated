package model

import org.joda.time.DateTime
import scala.util.Random

object AuthToken extends IdImplicitLike {
  import org.joda.time.format.DateTimeFormat
  private lazy val fmt = DateTimeFormat.forPattern("yyyy-MM-dd '@' HH:mm:ss")

  private val random = Random
  def nextId: Id = random.nextLong.toId
}

/** A token to authenticate a user for a short time.
 * @param id Unique token ID.
 * @param uid The unique ID of the user the token is associated with.
 * @param expiry The DateTime the token expires. */
case class AuthToken(
  uid: Id,
  expiry: DateTime,
  id: Id = AuthToken.nextId
) {
  override def toString = s"AuthToken: uid=$uid, expiry: ${ AuthToken.fmt.print(expiry) }, id='$id'"
}
