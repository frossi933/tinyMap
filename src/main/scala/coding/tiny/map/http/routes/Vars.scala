package coding.tiny.map.http.routes

import coding.tiny.map.model.tinyMap.TinyMapId
import eu.timepit.refined.refineV
import eu.timepit.refined.string.Uuid

object Vars {

  object TinyMapIdVar {
    def unapply(str: String): Option[TinyMapId] = {
      refineV[Uuid](str).toOption.map(TinyMapId(_))
    }
  }
}
