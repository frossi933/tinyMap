package coding.tiny.map.http.routes

import eu.timepit.refined.types.string.NonEmptyString

object Vars {

  object NonEmptyStringVar {
    def unapply(str: String): Option[NonEmptyString] = {
      if (str.nonEmpty)
        NonEmptyString.from(str).toOption
      else
        None
    }
  }
}
