package coding.tiny.map.services

import cats.effect.Sync
import cats.implicits._
import coding.tiny.map.collections.UndiGraphHMap
import coding.tiny.map.model.tinyMap.{City, Distance, TinyMap, TinyMapId}
import coding.tiny.map.repositories.TinyMapsRepository
import coding.tiny.map.services.TinyMapsService.TinyMapNotFoundException

class LiveTinyMapsService[F[_]](tinyMapsRepository: TinyMapsRepository[F])(implicit ev: Sync[F])
    extends TinyMapsService[F] {

  override def create(mapGraph: UndiGraphHMap[City, Distance]): F[TinyMap] =
    tinyMapsRepository.save(mapGraph)

  override def getAll: F[List[TinyMap]] = tinyMapsRepository.getAll

  override def getById(id: TinyMapId): F[TinyMap] = tinyMapsRepository.getById(id).flatMap {
    case Some(tmap) => tmap.pure[F]
    case None       => TinyMapNotFoundException(id).raiseError[F, TinyMap]
  }

  override def update(
      tmap: TinyMap,
      mapGraph: UndiGraphHMap[City, Distance]
  ): F[TinyMap] = {
    val updatedGraph = tmap.graph.updated(mapGraph)
    tmap.copy(graph = updatedGraph).pure[F]
  }

  override def delete(
      tmap: TinyMap,
      mapGraph: UndiGraphHMap[City, Distance]
  ): F[TinyMap] = {
    val updatedGraph = tmap.graph.deleted(mapGraph)
    tmap.copy(graph = updatedGraph).pure[F]
  }

  def shortestDistance(tmap: TinyMap, start: City, end: City): F[Distance] =
    tmap.graph.shortestDistance(start, end).pure[F]
}

object LiveTinyMapsService {

  def apply[F[_]: Sync](tinyMapsRepository: TinyMapsRepository[F]): F[LiveTinyMapsService[F]] = {
    Sync[F].delay {
      new LiveTinyMapsService[F](tinyMapsRepository)
    }
  }
}
