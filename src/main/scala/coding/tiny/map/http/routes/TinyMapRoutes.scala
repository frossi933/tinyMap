package coding.tiny.map.http.routes

import cats.effect._
import cats.implicits._
import coding.tiny.map.http.Requests.{
  CreateOrUpdateRequest,
  ShortestDistanceRequest,
  ShortestDistanceResponse
}
import coding.tiny.map.http.routes.Vars.TinyMapIdVar
import coding.tiny.map.model.tinyMap.Distance._
import coding.tiny.map.model.tinyMap.TinyMap._
import coding.tiny.map.services.TinyMapsService
import coding.tiny.map.services.TinyMapsService.{
  ShortestDistanceException,
  TinyMapNotFoundException
}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Response}

case class TinyMapRoutes[F[_]: Sync](tinyMapsService: TinyMapsService[F]) {

  private[routes] val prefixPath = "/maps"

  val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
  import dsl._

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root =>
      tinyMapsService.getAll.flatMap(Ok(_))

    case req @ POST -> Root =>
      val responseF = for {
        createReqBody <- req.as[CreateOrUpdateRequest]
        mapGraph      <- createReqBody.toGraph
        tmap          <- tinyMapsService.create(mapGraph)
        response      <- Ok(tmap)
      } yield response
      recoverExceptions(responseF)

    case GET -> Root / TinyMapIdVar(id) =>
      val responseF = tinyMapsService
        .getById(id)
        .flatMap(Ok(_))
      recoverExceptions(responseF)

    case req @ PUT -> Root / TinyMapIdVar(id) =>
      val responseF = for {
        updateReqBody <- req.as[CreateOrUpdateRequest]
        graphUpdates  <- updateReqBody.toGraph
        tmap          <- tinyMapsService.getById(id)
        updated       <- tinyMapsService.update(tmap, graphUpdates)
        response      <- Ok(updated)
      } yield response
      recoverExceptions(responseF)

    case req @ DELETE -> Root / TinyMapIdVar(id) =>
      val responseF = for {
        deleteReqBody <- req.as[CreateOrUpdateRequest]
        graphToRemove <- deleteReqBody.toGraph
        tmap          <- tinyMapsService.getById(id)
        updated       <- tinyMapsService.delete(tmap, graphToRemove)
        response      <- Ok(updated)
      } yield response
      recoverExceptions(responseF)

    case req @ GET -> Root / TinyMapIdVar(id) / "shortestDistance" =>
      val responseF = for {
        shortestDistReq <- req.as[ShortestDistanceRequest]
        tmap            <- tinyMapsService.getById(id)
        dist            <- tinyMapsService.shortestDistance(tmap, shortestDistReq.start, shortestDistReq.end)
        response        <- Ok(ShortestDistanceResponse(dist))
      } yield response
      recoverExceptions(responseF)
  }

  private def recoverExceptions(result: F[Response[F]]) = result.recoverWith {
    case TinyMapNotFoundException(id)          =>
      NotFound(s"Tiny Map with id: $id doesn't exist")
    case ShortestDistanceException(start, end) =>
      BadRequest(
        s"It's not possible to calculate the shortest distance between $start and $end. Tiny Maps should represent connected graphs."
      )
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
