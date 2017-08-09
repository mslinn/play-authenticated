# Play Framework User Id / Password Authentication Seed Project

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://travis-ci.org/mslinn/play-authenticated.svg?branch=master)](https://travis-ci.org/mslinn/play-authenticated)
[![GitHub version](https://badge.fury.io/gh/mslinn%2Fplay-authenticated.svg)](https://badge.fury.io/gh/mslinn%2Fplay-authenticated)

This project uses Play 2.5's [AuthenticatedBuilder](https://www.playframework.com/documentation/2.5.x/api/scala/index.html#play.api.mvc.Security$$AuthenticatedBuilder) and
[Action composition](https://www.playframework.com/documentation/2.5.x/ScalaActionsComposition#Composing-actions)
to create user ID / password authentication.

This project is inspired by Jorge Aliss's [SecureSocial](http://www.securesocial.ws/),
and uses some code from [play-silhouette-seed](https://github.com/mohiva/play-silhouette-seed).
Instead of supporting authentication via social providers, it only addresses user id / password authentication.
Functionality includes sign up (registration), log in, log out, password reset for when the user forgets their password,
 the ability for authenticated users to change their password, and cleans up stale tokens and abandoned user signups.

Uses [Quill](http://getquill.io/) and an in-memory [H2](http://www.h2database.com/html/main.html) database.

Uses [WebJars](http://webjars.org/) with [Twitter Bootstrap](http://getbootstrap.com/)
and [HtmlForm](https://github.com/mslinn/html-form-scala)'s HTML5 widgets for Bootstrap.

It would be a lot of work to turn this into a library.
Instead, you could incorporate this code into a larger project as a subproject, and modify as required.

## Using Authentication-Aware Actions

In addition to Play Framework's `Action` handlers, this project adds
[UserAwareAction](http://blog.mslinn.com/play-authenticated/latest/api/index.html#auth.Authentication@UserAwareAction(f:auth.RequestWithUser[play.api.mvc.AnyContent]=>play.api.mvc.Result):play.api.mvc.Action[play.api.mvc.AnyContent])
and [SecuredAction](http://blog.mslinn.com/play-authenticated/latest/api/index.html#auth.Authentication@SecuredAction) handlers.
If you are familiar with SecureSocial, these work exactly the same as similarly named handlers in SecureSocial.
Here is an example of a [Controller](https://www.playframework.com/documentation/2.5.x/api/scala/index.html#play.api.mvc.Controller)
containing all 3 types of `Action` handlers:

```
package controllers

import javax.inject.Inject
import auth.Authentication
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

class ApplicationController @Inject() (implicit
  val messagesApi: MessagesApi,
  webJarAssets: WebJarAssets,
  authentication: Authentication
) extends Controller with I18nSupport {
  import authentication._

  def index() = Action { implicit request =>
    Ok(views.html.index())
  }

  def securedAction = SecuredAction { implicit authenticatedRequest =>
    val user: User = authenticatedRequest.user
    Ok(s"${ user.fullName } is secure.")
  }

  def userAwareAction = UserAwareAction { implicit requestWithUser =>
    val maybeUser: Option[User] = requestWithUser.user
    Ok(s"${ maybeUser.map(_.fullName).getOrElse("No-one") } is aware of their lack of security.")
  }
}
```

## Scaladoc
[Here](http://mslinn.github.io/play-authenticated/latest/api/index.html)

## To Run
Set environment variables that establish email server settings, then run the program.

    $ export SMTP_PASSWORD=myPassword
    $ export SMTP_USER=santa@claus.com
    $ export SMTP_FROM="Santa Claus <santa@claus.com>"
    $ export SMTP_HOST=smtp.claus.com
    $ export SMTP_PORT=465
    $ export EMAIL_LOGO_URL="http://siteassets.scalacourses.com/images/ScalaCoursesHeadingLogo371x56.png"
    $ export EMAIL_SIGNATURE="<p>Thank you,<br/></p>\n<p>The ScalaCourses mailbot</p>"
    $ sbt run

## Sponsor
This project is sponsored by [Micronautics Research Corporation](http://www.micronauticsresearch.com/),
the company that delivers online Scala and Play training via [ScalaCourses.com](http://www.ScalaCourses.com).
You can learn how this project works by taking the [Introduction to Scala](http://www.ScalaCourses.com/showCourse/40),
[Intermediate Scala](http://www.ScalaCourses.com/showCourse/45) and [Introduction to Play](http://www.ScalaCourses.com/showCourse/39) courses.

## License
This software is published under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
