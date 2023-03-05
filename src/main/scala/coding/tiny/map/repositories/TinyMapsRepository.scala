package coding.tiny.map.repositories

import coding.tiny.map.collections.UndiGraphHMap
import coding.tiny.map.model.tinyMap.{City, Distance, TinyMap, TinyMapId}

trait TinyMapsRepository[F[_]] {

  def save(mapGraph: UndiGraphHMap[City, Distance]): F[TinyMap]

  def update(tinyMap: TinyMap): F[TinyMap]

  def getAll: F[List[TinyMap]]

  def getById(name: TinyMapId): F[Option[TinyMap]]

}
