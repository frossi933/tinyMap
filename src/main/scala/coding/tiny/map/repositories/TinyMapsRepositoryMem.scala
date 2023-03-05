package coding.tiny.map.repositories

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import coding.tiny.map.collections.UndiGraphHMap
import coding.tiny.map.model.tinyMap.{City, Distance, TinyMap, TinyMapId}

import scala.collection.mutable

class TinyMapsRepositoryMem[F[_]: Applicative] extends TinyMapsRepository[F] {
  var tmaps: mutable.HashMap[TinyMapId, UndiGraphHMap[City, Distance]] =
    mutable.HashMap[TinyMapId, UndiGraphHMap[City, Distance]]()

  override def getAll: F[Vector[TinyMap]] =
    tmaps.toVector.map { case (id, graph) => TinyMap(id, graph) }.pure[F]

  override def getById(id: TinyMapId): F[Option[TinyMap]] =
    tmaps(id).some.map(g => TinyMap(id, g)).pure[F]

  override def save(mapGraph: UndiGraphHMap[City, Distance]): F[TinyMap] = {
    val id = TinyMapId.make
    tmaps = tmaps.addOne(id -> mapGraph)
    TinyMap(id, mapGraph).pure[F]
  }

  override def update(tmap: TinyMap): F[TinyMap] = {
    tmaps = tmaps.addOne(tmap.id -> tmap.graph)
    tmap.pure[F]
  }
}

object TinyMapsRepositoryMem {

  def make[F[_]: Sync](): F[TinyMapsRepositoryMem[F]] = Sync[F].delay {
    new TinyMapsRepositoryMem[F]
  }
}
