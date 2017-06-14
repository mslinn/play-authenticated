package auth

import model.{ClearTextPassword, EMail, Id, UserId}
import play.api.data.Forms.of
import play.api.data.format.Formats.doubleFormat
import play.api.data.format.Formatter
import play.api.data.{FormError, Mapping}

/** Play Framework form field mapping formatters.
  * To use, either mix in the `forms.FormatterLike` trait or import the `forms.Formatters` object. */
trait FormFormatterLike {
  implicit val emailFormat = new Formatter[EMail] {
    /** @param key indicates the name of the form field to convert from String to EMail
      * @param data is a Map of field name -> value */
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], EMail] =
      data
        .get(key)
        .map(EMail.apply)
        .toRight(Seq(FormError(key, "error.required", Nil)))

    def unbind(key: String, value: EMail): Map[String, String] = Map(key -> value.value)
  }

  implicit val idFormat = new Formatter[Id] {
    /** @param key indicates the name of the form field to convert from String to EMail
      * @param data is a Map of field name -> value */
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Id] =
      data
        .get(key)
        .map(k => Id.apply(k.toLong))
        .toRight(Seq(FormError(key, "error.required", Nil)))

    def unbind(key: String, value: Id): Map[String, String] = Map(key -> value.toString)
  }

  implicit val clearTextPasswordFormat = new Formatter[ClearTextPassword] {
    /** @param key indicates the name of the form field to convert from String to ClearTextPassword
      * @param data is a Map of field name -> value */
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], ClearTextPassword] =
      data
        .get(key)
        .map(ClearTextPassword.apply)
        .toRight(Seq(FormError(key, "error.required", Nil)))

    def unbind(key: String, value: ClearTextPassword): Map[String, String] = Map(key -> value.value)
  }

  implicit val userIdFormat = new Formatter[UserId] {
    /** @param key indicates the name of the form field to convert from String to UserId
      * @param data is a Map of field name -> value */
    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], UserId] =
      data
        .get(key)
        .map(UserId.apply)
        .toRight(Seq(FormError(key, "error.required", Nil)))

    def unbind(key: String, value: UserId): Map[String, String] = Map(key -> value.value)
  }

  val eMail: Mapping[EMail]                                = of[EMail]
  val double: Mapping[Double]                              = of(doubleFormat)
  val id: Mapping[Id]                                      = of[Id]
  val clearTextPasswordMapping: Mapping[ClearTextPassword] = of[ClearTextPassword]
  val userId: Mapping[UserId]                              = of[UserId]
}

object FormFormatters extends FormFormatterLike
