package model.dao

import java.util.UUID
import io.getquill.{Escape, H2JdbcContext, NamingStrategy, SnakeCase}
import model.persistence.Id
import org.joda.time.DateTime

object QuillImplicits {
  lazy val ctx: H2JdbcContext[TableNameSnakeCase] = new H2JdbcContext[TableNameSnakeCase]("quill")
}

trait QuillImplicits {
  import QuillImplicits.ctx._

  implicit val dateTimeDecoder: Decoder[DateTime] =
    decoder(java.sql.Types.TIMESTAMP, (index, row) => new DateTime(row.getTimestamp(index).getTime))

  implicit val dateTimeEncoder: Encoder[DateTime] =
    encoder(
      java.sql.Types.TIMESTAMP,
      (index, value, row) => row.setTimestamp(index, new java.sql.Timestamp(value.getMillis))
    )

  implicit val idLongDecoder: Decoder[Id[Long]] =
    decoder(java.sql.Types.BIGINT, (index, row) => Id(row.getLong(index)))

  implicit val idLongEncoder: Encoder[Id[Long]] =
    encoder(java.sql.Types.BIGINT, (index, value, row) => row.setLong(index, value.value))


  implicit val idOptionLongDecoder: Decoder[Id[Option[Long]]] =
    decoder(java.sql.Types.BIGINT, (index, row) => Id(Some(row.getLong(index))))

  implicit val idOptionLongEncoder: Encoder[Id[Option[Long]]] =
    encoder(java.sql.Types.BIGINT, (index, value, row) =>
      value.value match {
        case Some(v) => row.setLong(index, v)
        case None    => row.setNull(index, java.sql.Types.BIGINT)
      })

  implicit val idUuidDecoder: Decoder[Id[UUID]] =
    decoder(java.sql.Types.BIGINT, (index, row) => Id(UUID.fromString(row.getString(index))))

  implicit val idUuidEncoder: Encoder[Id[UUID]] =
    encoder(java.sql.Types.BIGINT, (index, value, row) => row.setString(index, value.value.toString))
}

/** Ensures that table names are quoted and snake_case but never start with a leading _. */
trait TableNameSnakeCase extends NamingStrategy with Escape with SnakeCase {
  override def table(s: String): String   = {
    val x = super.default(s)
    val y = if (x.startsWith("_")) x.substring(1) else x
    s""""$y""""
  }
}

object TableNameSnakeCase extends TableNameSnakeCase
