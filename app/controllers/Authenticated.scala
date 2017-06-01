package controllers

import models.User
import play.api.mvc.Security.AuthenticatedBuilder

object Authenticated extends AuthenticatedBuilder[User](req => AuthHelper.parseUserFromRequest(req))
