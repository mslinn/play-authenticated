import net.codingwell.scalaguice.ScalaModule
import javax.inject.Singleton
import auth.UnauthorizedHandler
import controllers.authentication.MyUnauthorizedHandler

class Modules extends ScalaModule {
  def configure(): Unit = {
    bind[UnauthorizedHandler].to[MyUnauthorizedHandler].in[Singleton]
  }
}
