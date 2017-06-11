# play-authenticated

An experiment with using Play 2.5's [AuthenticatedBuilder](https://www.playframework.com/documentation/2.5.x/api/scala/index.html#play.api.mvc.Security$$AuthenticatedBuilder) and
[Action composition](https://www.playframework.com/documentation/2.5.x/ScalaActionsComposition#Composing-actions)
to create user ID / password authentication.

Extensively modified from [David Keen's repo](https://gitlab.com/davidkeen/play-authenticated) of the same name,
with enhancements inspired by Jorge Aliss's [SecureSocial](http://www.securesocial.ws/).

Uses [Quill](http://getquill.io/) and an in-memory [H2](http://www.h2database.com/html/main.html) database.

Uses [WebJars](http://webjars.org/) with [Twitter Bootstrap](http://getbootstrap.com/) 
and [HtmlForm](https://github.com/mslinn/html-form-scala)'s HTML5 widgets for Bootstrap.

## To Run

    $ sbt run
