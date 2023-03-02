package coding.tiny.map.model

import cats.{Applicative, Hash, Monoid, Order}
import cats.implicits.catsSyntaxApplicativeId
import coding.tiny.map.collections.Graph.Edge
import coding.tiny.map.collections.{Graph, UndiGraphHMap}
import coding.tiny.map.http.Requests.TinyMapCityConnections
import coding.tiny.map.model.tinyMap.Road.toEdge
import eu.timepit.refined.types.numeric.{NonNegDouble, PosDouble}
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.JsonCodec
import io.circe.{Decoder, Encoder, HCursor}
import io.estatico.newtype.macros.newtype
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import io.circe.refined._

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

  @JsonCodec case class Road(from: City, to: City, distance: Distance)
  object Road {
    def fromEdge(edge: Edge[City, Distance]): Road = Road(edge.nodeA, edge.nodeB, edge.weight)

    def toEdge(road: Road): Edge[City, Distance] = Edge(road.from, road.to, road.distance)
  }

  @newtype case class TinyMapName(name: NonEmptyString)
  object TinyMapName {

    implicit val tmapNameDecoder: Decoder[TinyMapName]                      = deriving
    implicit val tmapNameEncoder: Encoder[TinyMapName]                      = deriving
    implicit def tmapNameEntityEncoder[F[_]]: EntityEncoder[F, TinyMapName] = jsonEncoderOf

  }

  final class TinyMap private (val graph: Graph[City, Distance]) {

    def cities: Vector[City] = graph.nodes
    def roads: Vector[Road]  = graph.edges.map(Road.fromEdge)

    def addCity(city: City): TinyMap                = new TinyMap(graph.insertNode(city))
    def addRoad(road: Road): TinyMap                = new TinyMap(graph.insertEdge(toEdge(road)))
    def deleteCity(city: City): TinyMap             = deleteCities(Vector(city))
    def deleteCities(cities: Vector[City]): TinyMap = new TinyMap(graph.removeNodes(cities))
    def deleteRoad(road: Road): TinyMap             = deleteRoads(Vector(road))
    def deleteRoads(roads: Vector[Road]): TinyMap   = new TinyMap(
      graph.removeEdges(roads.map(toEdge))
    )

    def shortestDistance(c1: City, c2: City): Distance = graph.shortestDistance(c1, c2)
  }

  object TinyMap {

    def apply[F[_]: Applicative](tmap: Vector[TinyMapCityConnections]): F[TinyMap] = {
      val edges: Vector[Edge[City, Distance]] = tmap.flatMap(cc =>
        cc.connections.toVector.map { case (city, dist) => Edge(cc.city, city, dist) }
      )
      // val a     = HashMap(tmap.map(n => n.city -> n.connections): _*)

      new TinyMap(UndiGraphHMap(edges)).pure
    }
  }
}
