package coding.tiny.map.model

import cats.{Hash, Monoid, Order}
import coding.tiny.map.collections.UndiGraphHMap
import coding.tiny.map.utils.RedisJsonCodec
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Uuid
import eu.timepit.refined.types.numeric.NonNegDouble
import eu.timepit.refined.types.string.NonEmptyString
import io.circe
import io.circe._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.refined._
import io.estatico.newtype.macros.newtype

import java.util.UUID

object tinyMap {

  @newtype final case class Distance(distance: NonNegDouble)
  object Distance {
    implicit val distDecoder: Decoder[Distance] = deriving
    implicit val distEncoder: Encoder[Distance] = deriving

    implicit val distMonoidInstance: Monoid[Distance] = Monoid.instance(
      Distance(NonNegDouble.unsafeFrom(0)),
      { case (d1, d2) => Distance(NonNegDouble.unsafeFrom(d1.distance.value + d2.distance.value)) }
    )
    implicit val distOrderInstance: Order[Distance]   = Order.by(_.distance.value)
  }

  @newtype final case class City(name: NonEmptyString)
  object City {
    implicit val cityDecoder: Decoder[City] = deriving
    implicit val cityEncoder: Encoder[City] = deriving

    implicit val cityKeyEncoder: KeyEncoder[City] = (city: City) => city.name.toString()
    implicit val cityKeyDecoder: KeyDecoder[City] = (key: String) =>
      NonEmptyString.from(key).map(City(_)).toOption

    implicit val cityHashInstance: Hash[City] = Hash.by(_.name.toString())
  }

  @newtype final case class TinyMapId(id: String Refined Uuid)
  object TinyMapId {

    def make: TinyMapId = TinyMapId(Refined.unsafeApply(UUID.randomUUID().toString))

    val allUuids: TinyMapId = TinyMapId(Refined.unsafeApply("*-*-*-*-*"))

    implicit val tmapIdDecoder: Decoder[TinyMapId] = deriving
    implicit val tmapIdEncoder: Encoder[TinyMapId] = deriving
    implicit val tmapIdCodec: Codec[TinyMapId]     = Codec.from(tmapIdDecoder, tmapIdEncoder)
  }

  final case class TinyMap(id: TinyMapId, graph: UndiGraphHMap[City, Distance])
  object TinyMap {

    import coding.tiny.map.collections.UndiGraphHMap._
    import coding.tiny.map.utils.CirceToRedisCodecs._

    implicit val decoderTinyMap: circe.Decoder[TinyMap] = deriveDecoder
    implicit val encoderTinyMap: circe.Encoder[TinyMap] = deriveEncoder

    val redisJsonCodec: RedisJsonCodec[TinyMapId, UndiGraphHMap[City, Distance]] =
      implicitly[RedisJsonCodec[TinyMapId, UndiGraphHMap[City, Distance]]]
  }
}
