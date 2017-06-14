import javax.inject.Singleton
import auth.UnauthorizedHandler
import controllers.authentication.MyUnauthorizedHandler
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
import service.AuthTokenCleaner

class Modules extends ScalaModule with AkkaGuiceSupport {
  def configure(): Unit = {
    bindActor[AuthTokenCleaner]("auth-token-cleaner")
    bind[UnauthorizedHandler].to[MyUnauthorizedHandler].in[Singleton]
  }
}
