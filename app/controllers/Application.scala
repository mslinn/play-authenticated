package controllers

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

class Application @Inject() (
  implicit val messagesApi: MessagesApi
) extends Controller with I18nSupport {
  def index = Action {
    Ok(views.html.index("Experimenting With Play Authentication"))
  }
}
