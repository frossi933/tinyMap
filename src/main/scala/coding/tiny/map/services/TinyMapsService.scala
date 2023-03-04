package coding.tiny.map.services

import coding.tiny.map.collections.UndiGraphHMap
import coding.tiny.map.model.tinyMap.{City, Distance, TinyMap, TinyMapId}

import scala.util.control.NoStackTrace

trait TinyMapsService[F[_]] {

  def create(mapGraph: UndiGraphHMap[City, Distance]): F[TinyMap]

  def getAll: F[List[TinyMap]]
  def getById(id: TinyMapId): F[TinyMap]

  def update(tmap: TinyMap, mapGraph: UndiGraphHMap[City, Distance]): F[TinyMap]
  def delete(tmap: TinyMap, mapGraph: UndiGraphHMap[City, Distance]): F[TinyMap]

  def shortestDistance(tmap: TinyMap, start: City, end: City): F[Distance]
}

object TinyMapsService {

  trait TinyMapsException                            extends NoStackTrace
  case class TinyMapNotFoundException(id: TinyMapId) extends TinyMapsException

}
