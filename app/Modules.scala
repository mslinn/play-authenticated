import models.{MyUnauthorizedHandler, UnauthorizedHandler}
import net.codingwell.scalaguice.ScalaModule
import javax.inject.Singleton

class Modules extends ScalaModule {
  def configure(): Unit = {
    bind[UnauthorizedHandler].to[MyUnauthorizedHandler].in[Singleton]
  }
}
