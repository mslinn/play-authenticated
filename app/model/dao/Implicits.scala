package model.dao

import io.getquill.H2JdbcContext
import model.Id
import org.joda.time.DateTime

trait Implicits {
  val ctx: H2JdbcContext[TableNameSnakeCase]
  import ctx._

  implicit val dateTimeDecoder: Decoder[DateTime] =
    decoder(java.sql.Types.TIMESTAMP, (index, row) => new DateTime(row.getTimestamp(index).getTime))

  implicit val dateTimeEncoder: Encoder[DateTime] =
    encoder(
      java.sql.Types.TIMESTAMP,
      (index, value, row) => row.setTimestamp(index, new java.sql.Timestamp(value.getMillis))
    )

  implicit val idDecoder: Decoder[Id] =
    decoder(java.sql.Types.BIGINT, (index, row) => Id(row.getLong(index)))

  implicit val idEncoder: Encoder[Id] =
    encoder(java.sql.Types.BIGINT, (index, value, row) => row.setLong(index, value.value))
}
