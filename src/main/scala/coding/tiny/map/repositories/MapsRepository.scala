package coding.tiny.map.repositories

import coding.tiny.map.model.tinyMap.{TinyMap, TinyMapName}

trait MapsRepository[F[_]] {

  def save(tmap: TinyMap): F[TinyMapName]

  def getAll(): F[List[TinyMap]]
  def getByName(name: TinyMapName): F[TinyMap]

}
