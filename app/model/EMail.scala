package model

import java.net.URLEncoder

object EMail {
  import com.micronautics.{EMailConfig, Smtp}
  import java.util.regex.Pattern
  import play.api.libs.json._

  val emailRegex: Pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)

  lazy val empty = EMail("x@y.com")

  implicit val emailFormatter: OFormat[EMail] = Json.format[EMail]

  protected val emailLogoUrl = "http://siteassets.scalacourses.com/images/ScalaCoursesHeadingLogo371x56.png"
  lazy val smtp: Smtp = Smtp.apply.smtp.copy(
    maybeEmailLogoUrl = Some(emailLogoUrl),
    maybeSignature = Some("<i><b>ScalaCourses</b><br/>World-class online training</i>")
  )
  lazy val emailConfig: EMailConfig = EMailConfig(smtp)

  def send(to: EMail, subject: String, cc: List[EMail]=Nil, bcc: List[EMail]=Nil)(body: String=""): Unit =
    emailConfig.smtp.send(mailTo=to.value, mailCc = bcc.map(_.value), mailBcc = bcc.map(_.value), subjectLine=subject, mailBody=body)
}

case class EMail(value: String) extends AnyVal {
  def isValid: Boolean = EMail.emailRegex.matcher(value).find

  def link(asCode: Boolean=true): String =
    s"${ if (asCode) "<code>" else "" }<a href='mailto:$value'>$value</a>${ if (asCode) "</code>" else "" }"

  /** Generates a mailto: link with the optional subject and/or body. The subject and/or body will be URLEncoded. */
  def mailTo(subject: String="", body: String=""): String = {
    import java.nio.charset.StandardCharsets.UTF_8
    val queryString = if ((subject + body).trim.isEmpty) "" else "?" +
      (if (subject.trim.isEmpty) "" else "subject=" + URLEncoder.encode(subject.trim, UTF_8.toString)) +
      (if (subject.nonEmpty && body.nonEmpty) "&" else "") +
      (if (body.trim.isEmpty) "" else "body=" + URLEncoder.encode(body.trim, UTF_8.toString))
    s"""mailto:${ link() }$queryString"""
  }

  def validate: EMail = {
    assert(isValid)
    EMail(value.trim.toLowerCase)
  }

  override def toString: String = validate.value
}

/** @return only the user name if the email is invalid or missing */
case class EMailFullName(name: String, email: EMail) {
  override def toString: String = if (email.isValid) s"$name <$email>" else name
}
