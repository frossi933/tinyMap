package coding.tiny.map.model

import cats.{Hash, Monoid, Order}
import coding.tiny.map.collections.Graph.Edge
import coding.tiny.map.collections.UndiGraphHMap
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Uuid
import eu.timepit.refined.types.numeric.NonNegDouble
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.refined._
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype

import java.util.UUID

object tinyMap {

  @newtype case class Distance(distance: NonNegDouble)
  object Distance {
    implicit val distDecoder: Decoder[Distance] = deriving
    implicit val distEncoder: Encoder[Distance] = deriving

    implicit val distMonoidInstance: Monoid[Distance] = Monoid.instance(
      Distance(NonNegDouble.unsafeFrom(0)),
      { case (d1, d2) => Distance(NonNegDouble.unsafeFrom(d1.distance.value + d2.distance.value)) }
    )
    implicit val distOrderInstance: Order[Distance]   = Order.by(_.distance.value)
  }

  @newtype case class City(name: NonEmptyString)
  object City {
    implicit val cityDecoder: Decoder[City] = deriving
    implicit val cityEncoder: Encoder[City] = deriving

    implicit val cityHashInstance: Hash[City] = Hash.by(_.name.toString())
  }

  // TODO remove?
  case class Road(from: City, to: City, distance: Distance)
  object Road {
    def fromEdge(edge: Edge[City, Distance]): Road = Road(edge.nodeA, edge.nodeB, edge.weight)

    def toEdge(road: Road): Edge[City, Distance] = Edge(road.from, road.to, road.distance)
  }

  @newtype case class TinyMapId(id: String Refined Uuid)
  object TinyMapId {

    def fromUUID(uuid: UUID): TinyMapId = TinyMapId(Refined.unsafeApply(uuid.toString))

    implicit val tmapNameDecoder: Decoder[TinyMapId] = deriving
    implicit val tmapNameEncoder: Encoder[TinyMapId] = deriving

  }

  final case class TinyMap(id: TinyMapId, graph: UndiGraphHMap[City, Distance])
  object TinyMap {
    implicit def decoderTinyMap(implicit
        grapDecoder: Decoder[UndiGraphHMap[City, Distance]]
    ): Decoder[TinyMap] = deriveDecoder

    implicit def encoderTinyMap(implicit
        grapEncoder: Encoder[UndiGraphHMap[City, Distance]]
    ): Encoder[TinyMap] = deriveEncoder
  }
}
