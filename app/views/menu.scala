package views.html

import controllers.authentication.routes.{AuthenticationController => AuthRoutes}
import controllers.routes.{ApplicationController => AppRoutes}
import play.api.mvc.{Call, RequestHeader}
import play.twirl.api.Html

object menu {
  protected def listItem(call: Call, linkText: String)(implicit request: RequestHeader): String = {
    val uri = call.url
    if (uri == request.uri) s"""<li class="active"><a href="#">$linkText</a></li>"""
    else s"""<li><a href="$uri">$linkText</a></li>"""
  }

  def apply(implicit request: RequestHeader) =
    Html(s"""<nav class="navbar navbar-default navbar-inverse navbar-static-top" role="navigation">
            |  <ul class="nav navbar-nav">
            |    ${listItem(AppRoutes.index(),               "Front page")}
            |    ${listItem(AppRoutes.userAwareAction(),     "UserAwareAction")}
            |    ${listItem(AppRoutes.securedAction(),       "SecuredAction")}
            |    ${listItem(AuthRoutes.signUpShow(),         "Sign up")}
            |    ${listItem(AuthRoutes.loginShow(),          "Log in")}
            |    ${listItem(AuthRoutes.showAccountDetails(), "Accounts")}
            |    ${listItem(AuthRoutes.passwordChangeShow(), "Change Password")}
            |    ${listItem(AuthRoutes.passwordForgotShow(), "Forgot Password")}
            |    ${listItem(AuthRoutes.logout(),             "Log out")}
            |  </ul>
            |</nav>""".stripMargin)
}
