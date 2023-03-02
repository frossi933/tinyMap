package coding.tiny.map.services

import cats.effect.Sync
import cats.implicits._
import coding.tiny.map.http.Requests
import coding.tiny.map.model.tinyMap.{City, Distance, Road, TinyMap, TinyMapId}
import coding.tiny.map.repositories.TinyMapsRepository
import coding.tiny.map.services.TinyMapsService.TinyMapNotFoundException

class LiveTinyMapsService[F[_]](tinyMapsRepository: TinyMapsRepository[F])(implicit ev: Sync[F])
    extends TinyMapsService[F] {

  override def create(tmap: TinyMap): F[TinyMapId] = tinyMapsRepository.save(tmap)

  override def getAll: F[List[TinyMap]] = tinyMapsRepository.getAll

  override def getById(id: TinyMapId): F[TinyMap] = tinyMapsRepository.getById(id).flatMap {
    case Some(tmap) => tmap.pure[F]
    case None       => TinyMapNotFoundException(id).raiseError[F, TinyMap]
  }

  override def update(tmap: TinyMap, cityConnections: Requests.TinyMapCityConnections): F[TinyMap] =
    ??? // tmap.update

  override def delete(
      tmap: TinyMap,
      cityConnections: Requests.TinyMapCityConnections
  ): F[TinyMap] = {
    val roads = cityConnections.connections.map { case (city, dist) =>
      Road(cityConnections.city, city, dist)
    }.toVector
    tmap.deleteRoads(roads).pure[F]
  }

  def shortestDistance(tmap: TinyMap, start: City, end: City): F[Distance] =
    tmap.shortestDistance(start, end).pure[F]
}

object LiveTinyMapsService {

  def apply[F[_]: Sync](tinyMapsRepository: TinyMapsRepository[F]): F[LiveTinyMapsService[F]] = {
    Sync[F].delay {
      new LiveTinyMapsService[F](tinyMapsRepository)
    }
  }
}
