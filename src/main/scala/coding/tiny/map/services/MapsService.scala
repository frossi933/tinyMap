package coding.tiny.map.services

import coding.tiny.map.http.Requests.TinyMapCityConnections
import coding.tiny.map.model.tinyMap.{City, Distance, TinyMap, TinyMapName}

trait MapsService[F[_]] {

  def create(tmap: TinyMap): F[TinyMapName]

  def getAll: F[List[TinyMap]]
  def getByName(name: TinyMapName): F[TinyMap]

  def update(tmap: TinyMap, cityConnections: TinyMapCityConnections): F[TinyMap]
  def delete(tmap: TinyMap, cityConnections: TinyMapCityConnections): F[TinyMap]

  def shortestDistance(tmap: TinyMap, start: City, end: City): F[Distance]
}
