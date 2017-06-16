package model

import java.util.UUID
import model.persistence.{Id, IdImplicitLike}
import play.api.mvc.{PathBindable, QueryStringBindable}

object PlayIdBinders extends IdImplicitLike {
  //// Id[Option[Long] converters
  implicit def objectQueryStringBindableIdOptionLong = new QueryStringBindable[Id[Option[Long]]] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Id[Option[Long]]]] =
      params.get(key).flatMap(_.headOption).map { value =>
        if (Id.isValid(value))
          Right(Id(Some(value.toLong)))
        else
          Left("Cannot parse parameter " + key + " as Id")
      }

    def unbind(key: String, value: Id[Option[Long]]): String = key + "=" + value.toString
  }

  implicit def pathBinderStringIdOptionLong(implicit binder: PathBindable[String]) = new PathBindable[Id[Option[Long]]] {
    override def bind(key: String, value: String): Either[String, Id[Option[Long]]] =
      try {
        for { idStr <- binder.bind(key, value).right } yield idStr.toId[Option[Long]]
      } catch { case e: Exception => Left(e.getMessage) }

    override def unbind(key: String, id: Id[Option[Long]]): String =
      binder.unbind(key, id.toString)
  }

  implicit object bindableIdIdOptionLong extends PathBindable.Parsing[Id[Option[Long]]](
    _.toId, _.toString, (key: String, e: Exception) => s"Cannot parse parameter $key as Id: ${ e.getMessage }}"
  )


  //// Id[UUID] converters

  implicit def objectQueryStringBindableIdUuid = new QueryStringBindable[Id[UUID]] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Id[UUID]]] =
      params.get(key).flatMap(_.headOption).map { value =>
        if (Id.isValid(value))
          Right(Id(UUID.fromString(value)))
        else
          Left("Cannot parse parameter " + key + " as Id")
      }

    def unbind(key: String, value: Id[UUID]): String = key + "=" + value.toString
  }

  implicit def pathBinderStringIdUuid(implicit binder: PathBindable[String]) = new PathBindable[Id[UUID]] {
    override def bind(key: String, value: String): Either[String, Id[UUID]] =
      try {
        for { idStr <- binder.bind(key, value).right } yield idStr.toId[UUID]
      } catch { case e: Exception => Left(e.getMessage) }

    override def unbind(key: String, id: Id[UUID]): String =
      binder.unbind(key, id.toString)
  }

  implicit object bindableIdIdUuid extends PathBindable.Parsing[Id[UUID]](
    _.toId, _.toString, (key: String, e: Exception) => s"Cannot parse parameter $key as Id: ${ e.getMessage }}"
  )
}
