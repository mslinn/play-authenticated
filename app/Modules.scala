import net.codingwell.scalaguice.ScalaModule
import javax.inject.Singleton
import authentication.UnauthorizedHandler
import controllers.MyUnauthorizedHandler

class Modules extends ScalaModule {
  def configure(): Unit = {
    bind[UnauthorizedHandler].to[MyUnauthorizedHandler].in[Singleton]
  }
}
