package service

import javax.inject.Inject
import akka.actor._
import model.dao.{AuthTokens, Users}
import model.{AuthToken, User}
import play.api.Logger
import service.AuthTokenCleaner.Clean
import scala.concurrent.ExecutionContext
import scala.language.postfixOps

/** Delete stale AuthTokens and Users that never authenticated. */
class AuthTokenCleaner @Inject() (
  authTokenScheduler: AuthTokenScheduler,
  users: Users
)( implicit
  executionContext: ExecutionContext
) extends Actor {
  def receive: Receive = {
    case Clean =>
      val staleTokens: Vector[AuthToken] = AuthTokens.findExpired(authTokenScheduler.expired)
      val staleUsers:  Vector[User]      = staleTokens.flatMap(token => users.findById(token.uid))
                                                      .filterNot(_.activated)

      staleTokens.foreach(AuthTokens.delete)
      Logger.info(s"${ staleTokens.size } auth tokens(s) were deleted.")

      staleUsers.foreach(users.delete)
      Logger.info(s"${ staleUsers.size } users(s) were deleted.")

      Logger.info(s"Next cleanup will be at ${ authTokenScheduler.nextCleanupFormatted }.")
  }
}

object AuthTokenCleaner {
  case object Clean
}
