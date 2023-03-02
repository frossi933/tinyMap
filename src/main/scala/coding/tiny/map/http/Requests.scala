package coding.tiny.map.http

import coding.tiny.map.model.tinyMap.{City, Distance}
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json, KeyDecoder, KeyEncoder}
import io.circe.generic.JsonCodec

object Requests {

  case class TinyMapCityConnections(city: City, connections: Map[City, Distance])
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

    implicit val cityKeyEncoder: KeyEncoder[City]                 = (city: City) => city.name.toString()
    implicit val cityKeyDecoder: KeyDecoder[City]                 = (key: String) =>
      NonEmptyString.from(key).toOption.map(nes => City(nes))
    implicit val mapCityDistDecoder: Decoder[Map[City, Distance]] =
      Decoder.decodeMap[City, Distance]
    implicit val mapCityDistEncoder: Encoder[Map[City, Distance]] =
      Encoder.encodeMap[City, Distance]
  }

  @JsonCodec case class CreateRequest(`map`: Vector[TinyMapCityConnections])

  @JsonCodec case class ShortestDistanceRequest(start: City, end: City)

}
