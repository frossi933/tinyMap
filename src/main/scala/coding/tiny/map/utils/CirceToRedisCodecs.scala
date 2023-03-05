package coding.tiny.map.utils

import dev.profunktor.redis4cats.codecs.splits.SplitEpi
import dev.profunktor.redis4cats.codecs.{Codecs => RedisCodecs}
import dev.profunktor.redis4cats.data.RedisCodec
import io.circe
import io.circe.parser.parse

trait SplitEpiStringTo[T] {
  val stringSplitEpi: SplitEpi[String, T]
}

trait RedisJsonCodec[K, T] {
  val redisCodec: RedisCodec[K, T]
}

object CirceToRedisCodecs {

  implicit def circeSplitEpiStringTo[T](implicit tCirceCodec: circe.Codec[T]): SplitEpiStringTo[T] =
    new SplitEpiStringTo[T] {
      override val stringSplitEpi: SplitEpi[String, T] = SplitEpi(
        s => parse(s).toOption.flatMap(json => tCirceCodec.decodeJson(json).toOption).get,
        t => tCirceCodec.apply(t).toString()
      )
    }

  implicit def circeRedisJsonCodec[K, V](implicit
      stringToK: SplitEpiStringTo[K],
      stringToV: SplitEpiStringTo[V]
  ): RedisJsonCodec[K, V] = new RedisJsonCodec[K, V] {
    override val redisCodec: RedisCodec[K, V] =
      RedisCodecs.derive(RedisCodec.Utf8, stringToK.stringSplitEpi, stringToV.stringSplitEpi)
  }
}
