package controllers

import javax.inject.Inject
import auth.Authentication
import model.User
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

object ApplicationController {
  val title = "Play Authentication Experiment"
}

class ApplicationController @Inject() (implicit
  val messagesApi: MessagesApi,
  webJarAssets: WebJarAssets,
  authentication: Authentication
) extends Controller with I18nSupport {
  import authentication._

  def index() = Action { implicit request =>
    Ok(views.html.index("Don't Worry, Be Happy"))
  }

  def securedAction = SecuredAction { implicit authenticatedRequest =>
    val user: User = authenticatedRequest.user
    Ok(views.html.index(s"${ user.fullName } is secure."))
  }

  def userAwareAction = UserAwareAction { implicit requestWithUser =>
    val maybeUser: Option[User] = requestWithUser.user
    Ok(views.html.index(s"${ maybeUser.map(_.fullName).getOrElse("No-one") } is aware of their lack of security."))
  }
}
