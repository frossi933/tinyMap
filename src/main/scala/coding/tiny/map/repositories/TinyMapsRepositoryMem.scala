package coding.tiny.map.repositories

import cats.Applicative
import cats.effect.Sync
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxOptionId}
import coding.tiny.map.collections.UndiGraphHMap
import coding.tiny.map.model.tinyMap.{City, Distance, TinyMap, TinyMapId}

import java.util.UUID
import scala.collection.mutable

class TinyMapsRepositoryMem[F[_]: Applicative] extends TinyMapsRepository[F] {
  var tmaps: mutable.HashMap[TinyMapId, UndiGraphHMap[City, Distance]] =
    mutable.HashMap[TinyMapId, UndiGraphHMap[City, Distance]]()

  override def getAll: F[List[TinyMap]] =
    tmaps.toList.map { case (id, graph) => TinyMap(id, graph) }.pure[F]

  override def getById(id: TinyMapId): F[Option[TinyMap]] =
    tmaps(id).some.map(g => TinyMap(id, g)).pure[F]

  override def save(mapGraph: UndiGraphHMap[City, Distance]): F[TinyMap] = {
    val id = TinyMapId.fromUUID(UUID.randomUUID())
    tmaps = tmaps.addOne(id -> mapGraph)
    TinyMap(id, mapGraph).pure[F]
  }
}

object TinyMapsRepositoryMem {

  def apply[F[_]: Sync](): F[TinyMapsRepositoryMem[F]] = new TinyMapsRepositoryMem[F].pure
}
