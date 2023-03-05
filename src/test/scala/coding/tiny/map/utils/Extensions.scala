package coding.tiny.map.utils

import org.scalatest.Assertions.fail

object Extensions {

  implicit class OptionOps[A](o: Option[A]) {

    def getOrFail: A = o.fold(fail("Trying to get a value from a None object"))(identity)
  }
}
