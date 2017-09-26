package model

import java.util.UUID
import com.github.nscala_time.time.Imports._
import model.persistence.{Id, IdImplicitLike}
import model.persistence.Types._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter

object AuthToken extends IdImplicitLike {
  import org.joda.time.format.DateTimeFormat
  lazy val fmt: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd '@' HH:mm:ss")
}

/** A token to authenticate a user for a short time.
 * @param id Unique token ID.
 * @param uid The unique ID of the user the token is associated with; type Id[Option[Long]\] is easier to work with than Id[Long]
 * @param expiry The DateTime the token expires. */
case class AuthToken(
  uid: IdOptionLong,
  expiry: DateTime = DateTime.now + 3.hours,
  id: IdUuid = Id(UUID.randomUUID)
) {
  def isValid: Boolean = expiry.isAfterNow

  override def toString = s"AuthToken: uid=$uid, expiry: ${ AuthToken.fmt.print(expiry) }, id='$id'"
}
