package model.dao

import io.getquill.H2JdbcContext
import model.persistence.{QuillCacheImplicits, TableNameSnakeCase}

object QuillImplicits {
  lazy val ctx: H2JdbcContext[TableNameSnakeCase] with QuillCacheImplicits =
    new H2JdbcContext[TableNameSnakeCase]("quill") with QuillCacheImplicits
}
