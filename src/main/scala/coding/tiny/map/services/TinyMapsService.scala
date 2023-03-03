package coding.tiny.map.services

import coding.tiny.map.http.Requests.{CreateOrUpdateRequest, TinyMapCityConnections}
import coding.tiny.map.model.tinyMap.{City, Distance, TinyMap, TinyMapId}

import scala.util.control.NoStackTrace

trait TinyMapsService[F[_]] {

  def create(tmap: TinyMap): F[TinyMapId]

  def getAll: F[List[TinyMap]]
  def getById(id: TinyMapId): F[TinyMap]

  def update(tmap: TinyMap, cityConnections: CreateOrUpdateRequest): F[TinyMap]
  def delete(tmap: TinyMap, cityConnections: TinyMapCityConnections): F[TinyMap]

  def shortestDistance(tmap: TinyMap, start: City, end: City): F[Distance]
}

object TinyMapsService {

  trait TinyMapsException                            extends NoStackTrace
  case class TinyMapNotFoundException(id: TinyMapId) extends TinyMapsException

}
