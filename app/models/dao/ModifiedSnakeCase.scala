package models.dao

import io.getquill.{Escape, NamingStrategy, SnakeCase}

/** Ensures that table names are quoted and snake_case but never start with a leading _. */
trait ModifiedSnakeCase extends NamingStrategy with Escape with SnakeCase {
  override def table(s: String): String   = {
    val x = super.default(s)
    val y = if (x.startsWith("_")) x.substring(1) else x
    s""""$y""""
  }
}

object ModifiedSnakeCase extends ModifiedSnakeCase
