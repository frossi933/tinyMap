package coding.tiny.map.http

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import coding.tiny.map.collections.Graph.Edge
import coding.tiny.map.collections.UndiGraphHMap
import coding.tiny.map.model.tinyMap.{City, Distance}
import eu.timepit.refined.types.string.NonEmptyString
import io.circe._
import io.circe.generic.JsonCodec

object Requests {

  final case class TinyMapCityConnections(city: City, connections: Map[City, Distance]) {

    def toEdges: Vector[Edge[City, Distance]] = connections.map { case (cityConn, dist) =>
      Edge(city, cityConn, dist)
    }.toVector

  }

  object TinyMapCityConnections {

    implicit val encodeTMapCityConn: Encoder[TinyMapCityConnections] =
      (a: TinyMapCityConnections) =>
        Json.obj(
          (a.city.name.toString(), mapCityDistEncoder(a.connections))
        )

    implicit val decodeTMapCityConn: Decoder[TinyMapCityConnections] = (c: HCursor) =>
      for {
        city        <- c.keys
                         .flatMap(_.headOption)
                         .filter(_.nonEmpty)
                         .toRight(DecodingFailure("Wrong city name", List()))
        connections <- c.downField(city).as[Map[City, Distance]]
      } yield {
        new TinyMapCityConnections(City(NonEmptyString.unsafeFrom(city)), connections)
      }

    implicit val mapCityDistDecoder: Decoder[Map[City, Distance]] =
      Decoder.decodeMap[City, Distance]
    implicit val mapCityDistEncoder: Encoder[Map[City, Distance]] =
      Encoder.encodeMap[City, Distance]
  }

  @JsonCodec final case class CreateOrUpdateRequest(`map`: Vector[TinyMapCityConnections]) {
    def toGraph[F[_]: Applicative]: F[UndiGraphHMap[City, Distance]] = {
      val edges = `map`.flatMap(_.toEdges)
      UndiGraphHMap.make(edges).pure[F]
    }
  }

  @JsonCodec final case class ShortestDistanceRequest(start: City, end: City)

  @JsonCodec final case class ShortestDistanceResponse(distance: Distance)
}
