package views.html

import controllers.routes.{AuthenticationController => AccountRoutes}
import controllers.routes.{ApplicationController => AppRoutes}
import play.api.mvc.{Call, RequestHeader}
import play.twirl.api.Html

object menu {
  protected def listItem(call: Call, linkText: String)(implicit request: RequestHeader): String = {
    val uri = call.url
    if (uri == request.uri) s"""<li class="active"><a href="#">$linkText</a></li>""" else s"""<li><a href="$uri">$linkText</a></li>"""
  }

  def apply(implicit request: RequestHeader) =
    Html(s"""<nav class="navbar navbar-default navbar-inverse navbar-static-top" role="navigation">
            |  <ul class="nav navbar-nav">
            |    ${listItem(AppRoutes.index(), "Front page")}
            |    ${listItem(AccountRoutes.signup(), "Sign up")}
            |    ${listItem(AccountRoutes.login(), "Log in")}
            |    ${listItem(AccountRoutes.showAccountDetails(), "Account Information")}
            |    ${listItem(AccountRoutes.logout(), "Log out")}
            |  </ul>
            |</nav>""".stripMargin)
}
