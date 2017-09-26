package controllers

import javax.inject.Inject
import auth.Authentication
import model.User
import org.webjars.play.WebJarAssets
import play.api.i18n.I18nSupport
import play.api.mvc._

object ApplicationController {
  val title = "Play User Id / Password Authentication"
}

class ApplicationController @Inject() (implicit
  authentication: Authentication,
  mcc: MessagesControllerComponents,
  webJarsUtil: org.webjars.play.WebJarsUtil
) extends MessagesAbstractController(mcc) with I18nSupport {
  import authentication._

  def index() = Action { implicit request =>
    Ok(views.html.index("Don't Worry, Be Happy"))
  }

  /** This action is invoked after a user logs in */
  def securedAction = SecuredAction { implicit authenticatedRequest =>
    val user: User = authenticatedRequest.user
    Ok(views.html.index(s"${ user.fullName } is secure."))
  }

  def userAwareAction: Action[AnyContent] = UserAwareAction { implicit requestWithUser =>
    val maybeUser: Option[User] = requestWithUser.user
    Ok(views.html.index(s"${ maybeUser.map(_.fullName).getOrElse("No-one") } is aware of their lack of security."))
  }
}
