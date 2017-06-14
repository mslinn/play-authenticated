import play.api.inject.guice.{GuiceApplicationBuilder, GuiceApplicationLoader}
import play.api.{ApplicationLoader, LoggerConfigurator}

/** Bog standard Play Framework application loader that also creates the in-memory database. */
class AppLoader extends GuiceApplicationLoader {
  override def builder(context: ApplicationLoader.Context): GuiceApplicationBuilder = {

    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment)
    }

    //models.dao.createDatabase(context.environment.getFile("conf/evolutions/default/1.sql"))

    initialBuilder
      .in(context.environment)
      .loadConfig(context.initialConfiguration)
      .overrides(overrides(context): _*)
  }
}
