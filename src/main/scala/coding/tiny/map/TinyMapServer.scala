package coding.tiny.map

import cats.Applicative
import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, Timer}
import cats.implicits._
import coding.tiny.map.http.routes.TinyMapRoutes
import coding.tiny.map.repositories.TinyMapsRepositoryDB
import coding.tiny.map.services.LiveTinyMapsService
import dev.profunktor.redis4cats.effect.{Log => R4CLogger}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object TinyMapServer {

  def stream[F[_]: ConcurrentEffect: ContextShift: R4CLogger: Applicative](implicit
      T: Timer[F]
  ): F[fs2.Stream[F, ExitCode]] = {
    for {
      mapsRepo    <- TinyMapsRepositoryDB()
      mapsService <- LiveTinyMapsService(mapsRepo)
      mapRoutes    = TinyMapRoutes(mapsService).routes
      httpApp      = Router("/" -> mapRoutes).orNotFound
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)
    } yield BlazeServerBuilder[F](global)
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(finalHttpApp)
      .serve
  }

}
