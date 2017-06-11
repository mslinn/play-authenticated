package controllers

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

class ApplicationController @Inject() (implicit
  val messagesApi: MessagesApi,
  webJarAssets: WebJarAssets
) extends Controller with I18nSupport {
  def index() = Action { implicit request =>
    Ok(views.html.index("Play Authentication Experiment"))
  }
}
