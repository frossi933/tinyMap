package coding.tiny.map.services

import cats.effect.Sync
import cats.implicits._
import coding.tiny.map.collections.UndiGraphHMap
import coding.tiny.map.model.tinyMap.{City, Distance, TinyMap, TinyMapId}
import coding.tiny.map.repositories.TinyMapsRepository
import coding.tiny.map.services.TinyMapsService.{
  ShortestDistanceException,
  TinyMapNotFoundException
}

class LiveTinyMapsService[F[_]: Sync](tinyMapsRepository: TinyMapsRepository[F])
    extends TinyMapsService[F] {

  override def create(mapGraph: UndiGraphHMap[City, Distance]): F[TinyMap] =
    tinyMapsRepository.save(mapGraph)

  override def getAll: F[Vector[TinyMap]] = tinyMapsRepository.getAll

  override def getById(id: TinyMapId): F[TinyMap] = tinyMapsRepository.getById(id).flatMap {
    case Some(tmap) => tmap.pure[F]
    case None       => TinyMapNotFoundException(id).raiseError[F, TinyMap]
  }

  override def update(
      tmap: TinyMap,
      mapGraph: UndiGraphHMap[City, Distance]
  ): F[TinyMap] = {
    val updatedTmap = tmap.copy(graph = tmap.graph.updated(mapGraph))
    tinyMapsRepository.update(updatedTmap)
  }

  override def delete(
      tmap: TinyMap,
      mapGraph: UndiGraphHMap[City, Distance]
  ): F[TinyMap] = {
    val updatedTmap = tmap.copy(graph = tmap.graph.deleted(mapGraph))
    tinyMapsRepository.update(updatedTmap)
  }

  def shortestDistance(tmap: TinyMap, start: City, end: City): F[Distance] =
    tmap.graph.shortestDistance(start, end) match {
      case Some(dist) => dist.pure[F]
      case None       => ShortestDistanceException(start, end).raiseError[F, Distance]
    }
}

object LiveTinyMapsService {

  def make[F[_]: Sync](tinyMapsRepository: TinyMapsRepository[F]): F[LiveTinyMapsService[F]] = {
    Sync[F].delay {
      new LiveTinyMapsService[F](tinyMapsRepository)
    }
  }
}
