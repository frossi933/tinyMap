package coding.tiny.map

import cats.effect.{BracketThrow, ConcurrentEffect, ContextShift, ExitCode, Timer}
import cats.implicits._
import coding.tiny.map.http.routes.TinyMapRoutes
import coding.tiny.map.repositories.TinyMapsRepositoryRedis
import coding.tiny.map.resources.AppResources
import coding.tiny.map.services.LiveTinyMapsService
import dev.profunktor.redis4cats.effect.{Log => R4CLogger}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

import scala.concurrent.ExecutionContext.global

object TinyMapServer {

  def stream[F[_]](appResources: AppResources[F])(implicit
      bracketThrow: BracketThrow[F],
      concurrentEffect: ConcurrentEffect[F],
      contextShift: ContextShift[F],
      logger: R4CLogger[F],
      T: Timer[F]
  ): F[fs2.Stream[F, ExitCode]] = {
    for {
      mapsRepo    <- TinyMapsRepositoryRedis(appResources.redis)
      mapsService <- LiveTinyMapsService(mapsRepo)
      mapRoutes    = TinyMapRoutes(mapsService).routes
      httpApp      = Router("/" -> mapRoutes).orNotFound
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)
    } yield BlazeServerBuilder[F](global)
      .bindHttp(appResources.blaze.port, appResources.blaze.host)
      .withHttpApp(finalHttpApp)
      .serve
  }

}
