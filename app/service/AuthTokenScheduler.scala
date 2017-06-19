package service

import akka.actor.{ActorRef, ActorSystem}
import com.github.nscala_time.time.Imports._
import com.google.inject.Inject
import com.google.inject.name.Named
import com.typesafe.config.{Config, ConfigFactory}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.{Configuration, Logger}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.language.postfixOps

class AuthTokenScheduler @Inject()(
  actorSystem: ActorSystem,
  @Named("auth-token-cleaner") authTokenCleanerRef: ActorRef,
  configuration: Configuration
)(implicit
  executionContext: ExecutionContext
) {
  val config: Config = Option(ConfigFactory.load.getConfig("authTokenCleaner"))
                                .getOrElse(ConfigFactory.parseString(
                                  """authTokenCleaner {
                                    |  period = 3 hours
                                    |  startDelay = 5 minutes
                                    |}
                                    |""".stripMargin))

  val staleTokenTimeout: FiniteDuration = Duration.fromNanos(config.getDuration("period").toNanos)

  val startDelay: FiniteDuration = Duration.fromNanos(config.getDuration("startDelay").toNanos)

  /** AuthTokens and Unauthenticated Users that were created before this DateTime are stale and should be deleted */
  def expired: DateTime = DateTime.now - staleTokenTimeout.toMillis

  /** DateTime for when AuthTokens and Unauthenticated Users that are created now will become stale */
  def expires: DateTime = DateTime.now + staleTokenTimeout.toMillis

  actorSystem.scheduler.schedule(startDelay, staleTokenTimeout, authTokenCleanerRef, AuthTokenCleaner.Clean)
  Logger.info(s"AuthToken and abandoned User cleanup scheduled in ${ startDelay } at " +
    DateTimeFormat.fullDateTime.print(durationToDateTime(startDelay)))

  def durationToDateTime(period: FiniteDuration): DateTime =
    DateTime.now.plus(org.joda.time.Duration.millis(period.toNanos/1000000))

  def nextCleanup: DateTime = durationToDateTime(staleTokenTimeout)

  def nextCleanupFormatted: String = DateTimeFormat.fullDateTime.print(nextCleanup)
}
