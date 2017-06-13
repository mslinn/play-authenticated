package model

import play.api.mvc.{PathBindable, QueryStringBindable}

object PlayUserIdBinders {
  implicit def objectIdQueryStringBindable = new QueryStringBindable[UserId] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, UserId]] =
      params.get(key).flatMap(_.headOption).map { value =>
        if (Id.isValid(value))
          Right(UserId(value))
        else
          Left("Cannot parse parameter " + key + " as UserId")
      }

    def unbind(key: String, value: UserId): String = key + "=" + value
  }

  implicit def userIdPathBinder(implicit binder: PathBindable[String]) = new PathBindable[UserId] {
    override def bind(key: String, value: String): Either[String, UserId] =
      try {
        for { idStr <- binder.bind(key, value).right } yield UserId(idStr)
      } catch { case e: Exception => Left(e.getMessage) }

    override def unbind(key: String, id: UserId): String =
      binder.unbind(key, id.value)
  }

  implicit object bindableUserId extends PathBindable.Parsing[UserId](
    UserId.apply, _.value, (key: String, e: Exception) => s"Cannot parse parameter $key as UserId: ${ e.getMessage }}"
  )
}
