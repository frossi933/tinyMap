package coding.tiny.map.http.routes

import cats._
import cats.effect._
import cats.implicits._
import cats.syntax._
import coding.tiny.map.http.Requests.{
  CreateRequest,
  ShortestDistanceRequest,
  TinyMapCityConnections
}
import coding.tiny.map.model.tinyMap.{TinyMap, TinyMapName}
import coding.tiny.map.http.routes.Vars.NonEmptyStringVar
import coding.tiny.map.services.MapsService
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import coding.tiny.map.model.tinyMap.Distance._
import org.http4s.server.Router

case class MapRoutes[F[_]: Sync](mapsService: MapsService[F]) {

  private[routes] val prefixPath = "/maps"

  val dsl = new Http4sDsl[F] {}
  import dsl._

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root =>
      mapsService.getAll.flatMap(ms => Ok(ms.map(_.roads))) // FIXME

    case req @ POST -> Root =>
      for {
        postReqBody <- req.as[CreateRequest]
        tmap        <- TinyMap[F](postReqBody.`map`)
        mapName     <- mapsService.create(tmap)
        res         <- Ok(mapName)
      } yield res

    case GET -> Root / NonEmptyStringVar(mapName) =>
      mapsService.getByName(TinyMapName(mapName)).flatMap(m => Ok(m.roads)) // FIXME

    case req @ PUT -> Root / NonEmptyStringVar(mapName) =>
      for {
        updateReqBody <- req.as[TinyMapCityConnections]
        tmap          <- mapsService.getByName(TinyMapName(mapName))
        updated       <- mapsService.update(tmap, updateReqBody)
        res           <- Ok(updated.roads) // FIXME
      } yield res

    case req @ DELETE -> Root / NonEmptyStringVar(mapName) =>
      for {
        deleteReqBody <- req.as[TinyMapCityConnections]
        tmap          <- mapsService.getByName(TinyMapName(mapName))
        updated       <- mapsService.delete(tmap, deleteReqBody)
        res           <- Ok(updated.roads) // FIXME
      } yield res

    case req @ GET -> Root / NonEmptyStringVar(mapName) / "shortestDistance" =>
      for {
        shortestDistReq <- req.as[ShortestDistanceRequest]
        tmap            <- mapsService.getByName(TinyMapName(mapName))
        dist             = mapsService.shortestDistance(tmap, shortestDistReq.start, shortestDistReq.end)
        res             <- Ok(dist) // FIXME
      } yield res
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
