package coding.tiny.map

import scala.concurrent.ExecutionContext.global
import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, Timer}
import coding.tiny.map.http.routes.MapRoutes
import coding.tiny.map.repositories.MapsRepositoryDB
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.Logger
import cats.syntax._
import cats.effect.syntax._
import cats.implicits._
import coding.tiny.map.services.LiveMapsService

object TinyMapServer {

  def stream[F[_]: ConcurrentEffect: ContextShift](implicit
      T: Timer[F]
  ): F[fs2.Stream[F, ExitCode]] = {
    for {
      mapsRepo    <- MapsRepositoryDB()
      mapsService <- LiveMapsService(mapsRepo)
      mapRoutes    = MapRoutes(mapsService).routes
      httpApp      = Router("/" -> mapRoutes).orNotFound
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)
    } yield BlazeServerBuilder[F](global)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(finalHttpApp)
      .serve
  }

}
