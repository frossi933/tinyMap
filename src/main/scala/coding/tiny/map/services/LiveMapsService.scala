package coding.tiny.map.services

import cats.effect.Sync
import cats.implicits.catsSyntaxApplicativeId
import coding.tiny.map.http.Requests
import coding.tiny.map.model.tinyMap.{City, Distance, Road, TinyMap, TinyMapName}
import coding.tiny.map.repositories.MapsRepository

class LiveMapsService[F[_]: Sync](mapsRepository: MapsRepository[F]) extends MapsService[F] {

  override def create(tmap: TinyMap): F[TinyMapName] = mapsRepository.save(tmap)

  override def getAll(): F[List[TinyMap]] = mapsRepository.getAll()

  override def getByName(name: TinyMapName): F[TinyMap] = mapsRepository.getByName(name)

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

object LiveMapsService {

  def apply[F[_]: Sync](mapsRepository: MapsRepository[F]): F[LiveMapsService[F]] = {
    Sync[F].delay {
      new LiveMapsService[F](mapsRepository)
    }
  }
}
