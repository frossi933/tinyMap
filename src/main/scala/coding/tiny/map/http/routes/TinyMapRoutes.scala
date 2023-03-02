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
import coding.tiny.map.model.tinyMap.{TinyMap, TinyMapId}
import coding.tiny.map.http.routes.Vars.TinyMapIdVar
import coding.tiny.map.services.TinyMapsService
import org.http4s.{HttpRoutes, Response}
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import coding.tiny.map.model.tinyMap.Distance._
import coding.tiny.map.services.TinyMapsService.TinyMapNotFoundException
import coding.tiny.map.http.Requests._
import org.http4s.server.Router

case class TinyMapRoutes[F[_]: Sync](tinyMapsService: TinyMapsService[F]) {

  private[routes] val prefixPath = "/maps"

  val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
  import dsl._

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root =>
      tinyMapsService.getAll.flatMap(ms => Ok(ms.map(_.roads))) // FIXME

    case req @ POST -> Root =>
      val responseF = for {
        postReqBody <- req.as[CreateRequest]
        tmap        <- TinyMap[F](postReqBody.`map`)
        mapName     <- tinyMapsService.create(tmap)
        response    <- Ok(mapName)
      } yield response
      recoverExceptions(responseF)

    case GET -> Root / TinyMapIdVar(id) =>
      val responseF = tinyMapsService
        .getById(id)
        .flatMap(m => Ok(m.roads)) // FIXME
      recoverExceptions(responseF)

    case req @ PUT -> Root / TinyMapIdVar(id) =>
      val responseF = for {
        updateReqBody <- req.as[TinyMapCityConnections]
        tmap          <- tinyMapsService.getById(id)
        updated       <- tinyMapsService.update(tmap, updateReqBody)
        response      <- Ok(updated.roads) // FIXME
      } yield response
      recoverExceptions(responseF)

    case req @ DELETE -> Root / TinyMapIdVar(id) =>
      val responseF = for {
        deleteReqBody <- req.as[TinyMapCityConnections]
        tmap          <- tinyMapsService.getById(id)
        updated       <- tinyMapsService.delete(tmap, deleteReqBody)
        response      <- Ok(updated.roads) // FIXME
      } yield response
      recoverExceptions(responseF)

    case req @ GET -> Root / TinyMapIdVar(id) / "shortestDistance" =>
      val responseF = for {
        shortestDistReq <- req.as[ShortestDistanceRequest]
        tmap            <- tinyMapsService.getById(id)
        dist             = tinyMapsService.shortestDistance(tmap, shortestDistReq.start, shortestDistReq.end)
        response        <- Ok(dist) // FIXME
      } yield response
      recoverExceptions(responseF)
  }

  private def recoverExceptions(result: F[Response[F]]) = result.recoverWith {
    case TinyMapNotFoundException(id) =>
      NotFound()
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
