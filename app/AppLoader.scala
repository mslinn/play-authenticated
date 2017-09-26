import play.api.{ApplicationLoader, LoggerConfigurator}
import _root_.play.api.inject.guice.{GuiceApplicationLoader, GuiceApplicationBuilder}

/** Bog standard Play Framework application loader that also creates the in-memory database. */
class AppLoader extends GuiceApplicationLoader {
  override def builder(context: ApplicationLoader.Context): GuiceApplicationBuilder = {

    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }

    model.dao.ProcessEvolutionUp(context.environment.getFile("conf/evolutions/default/1.sql"))

    initialBuilder
      .in(context.environment)
      .loadConfig(context.initialConfiguration)
      .overrides(overrides(context): _*)
  }
}
