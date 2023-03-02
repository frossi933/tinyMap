package coding.tiny.map.repositories

import coding.tiny.map.model.tinyMap.{TinyMap, TinyMapId}

trait TinyMapsRepository[F[_]] {

  def save(tmap: TinyMap): F[TinyMapId]

  def getAll: F[List[TinyMap]]

  def getById(name: TinyMapId): F[Option[TinyMap]]

}
