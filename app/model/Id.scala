package model

import com.micronautics.HasValue
import io.getquill.H2JdbcContext
import model.dao.TableNameSnakeCase
import play.api.libs.json._

/** To use, either import `IdImplicits._` or mix in IdImplicitLike */
trait IdImplicitLike {
  implicit class RichStringId(string: String) {
    def toId: Id = Id(string.toLong)
  }

  implicit class RichLongId(long: Long) {
    def toId: Id = Id(long)
  }

  implicit class RichIntId(int: Int) {
    def toId: Id = Id(int.toLong)
  }
}

object IdImplicits extends IdImplicitLike

object Id extends IdImplicitLike {
  def isValid(value: Long): Boolean = try {
    Id(value)
    true
  } catch {
    case _: Exception => false
  }

  def isValid(value: String): Boolean = try {
    Id(value.toLong)
    true
  } catch {
    case e: Exception => false
  }

  implicit val idJsonReads = new Reads[Id] {
    def reads(jsValue: JsValue): JsResult[Id] = JsSuccess(Id(jsValue.as[Long]))
  }

  implicit val idJsonWrites = new Writes[Id] {
    def writes(id: Id): JsValue = JsNumber(id.value)
  }

  def fromJson(jsString: JsString): Id = Id(jsString.as[Long])

  val zero: Id = Id(0L)
}

case class Id(value: Long) extends HasValue[Long] {
  def isNegative: Boolean = value < 0
  def isPositive: Boolean = value > 0
  def isZero: Boolean = value == 0
  def nonZero: Boolean = value != 0

  def toJson: JsValue = JsNumber(value)

  override def toString: String = value.toString
}
