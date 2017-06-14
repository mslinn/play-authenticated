import javax.inject.Singleton
import auth.UnauthorizedHandler
import controllers.authentication.MyUnauthorizedHandler
import net.codingwell.scalaguice.ScalaModule

class Modules extends ScalaModule {
  def configure(): Unit = {
    bind[UnauthorizedHandler].to[MyUnauthorizedHandler].in[Singleton]
  }
}
