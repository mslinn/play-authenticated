package model

import com.micronautics.HasValue
import model.persistence.Id
import play.api.libs.json._

trait IdJsonImplicits {
  implicit val idJsonReads = new Reads[Id[Long]] {
    def reads(jsValue: JsValue): JsResult[Id[Long]] = JsSuccess(Id(jsValue.as[Long]))
  }

  implicit val idJsonWrites = new Writes[Id[Long]] {
    def writes(id: Id[Long]): JsValue = JsNumber(id.value)
  }

  def fromJson(jsString: JsString): Id[Long] = Id(jsString.as[Long])

  implicit class RichId(override val value: Id[Long]) extends HasValue[Id[Long]] {
    def toJson: JsValue = JsNumber(value.value)
  }
}
