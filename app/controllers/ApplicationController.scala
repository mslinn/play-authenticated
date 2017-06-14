package controllers

import javax.inject.Inject
import auth.Authentication
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
    Ok(views.html.index())
  }

  def securedAction = SecuredAction { authenticatedRequest =>
    val user = authenticatedRequest.user
    Ok(s"${ user.fullName } is secure.")
  }

  def userAwareAction = UserAwareAction { requestWithUser =>
    val maybeUser = requestWithUser.user
    Ok(s"${ maybeUser.map(_.fullName).getOrElse("No-one") } is aware of their lack of security.")
  }
}
