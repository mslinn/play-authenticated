package model

import play.api.mvc.{PathBindable, QueryStringBindable}

object PlayIdBinders extends IdImplicitLike {
  implicit def objectIdQueryStringBindable = new QueryStringBindable[Id] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Id]] =
      params.get(key).flatMap(_.headOption).map { value =>
        if (Id.isValid(value))
          Right(Id(value.toLong))
        else
          Left("Cannot parse parameter " + key + " as Id")
      }

    def unbind(key: String, value: Id): String = key + "=" + value.toString
  }

  implicit def pathBinder(implicit binder: PathBindable[String]) = new PathBindable[Id] {
    override def bind(key: String, value: String): Either[String, Id] =
      try {
        for { idStr <- binder.bind(key, value).right } yield idStr.toId
      } catch { case e: Exception => Left(e.getMessage) }

    override def unbind(key: String, id: Id): String =
      binder.unbind(key, id.toString)
  }

  implicit object bindableId extends PathBindable.Parsing[Id](
    _.toId, _.toString, (key: String, e: Exception) => s"Cannot parse parameter $key as Id: ${ e.getMessage }}"
  )
}
