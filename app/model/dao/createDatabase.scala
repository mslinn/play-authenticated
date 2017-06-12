package model.dao

import java.io.File
import io.getquill.H2JdbcContext
import model.User
import scala.language.postfixOps

object createDatabase {
  lazy val ctx = new H2JdbcContext[TableNameSnakeCase]("quill")
  import ctx._

  def apply(file: File): Unit = {
    val evolution = scala.io.Source.fromFile(file).mkString
    println(evolution)
    //run { quote { infix"${ lift(evolution) }".as[BatchAction[User]] } } // does not compile. How to run DDL?
    ()
  }
}
