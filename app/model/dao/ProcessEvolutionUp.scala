package model.dao

import java.io.File
import io.getquill.H2JdbcContext
import scala.language.postfixOps

/** Extract the Up portion of Play Evolution file and execute those SQL statements */
object ProcessEvolutionUp {
  lazy val ctx = new H2JdbcContext[TableNameSnakeCase]("quill")
  import ctx._

  protected def contains(line: String, target: String): Boolean =
    line.toLowerCase.replaceAll("\\s+", " ") contains target

  def apply(file: File): Unit = {
    val evolutionLines = scala.io.Source.fromFile(file).getLines.toList
    val upString: String = evolutionLines
      .dropWhile(!contains(_, "# --- !ups"))
      .drop(1)
      .takeWhile(!contains(_, "# --- !downs"))
      .mkString("\n")
    executeAction(upString)
    ()
  }
}
