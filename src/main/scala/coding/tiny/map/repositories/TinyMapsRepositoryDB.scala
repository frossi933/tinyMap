package coding.tiny.map.repositories

import cats.Applicative
import cats.effect.Sync
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxOptionId}
import coding.tiny.map.model.tinyMap.{TinyMap, TinyMapId}
import eu.timepit.refined.refineV
import eu.timepit.refined.string.Uuid
import eu.timepit.refined.types.all.NonEmptyString

import java.util.UUID
import scala.collection.mutable

class TinyMapsRepositoryDB[F[_]: Applicative] extends TinyMapsRepository[F] {
  var tmaps: mutable.HashMap[TinyMapId, TinyMap] = mutable.HashMap[TinyMapId, TinyMap]()

  override def getAll: F[List[TinyMap]] = tmaps.toList.map(_._2).pure[F]

  override def getById(id: TinyMapId): F[Option[TinyMap]] = tmaps(id).some.pure[F]

  override def save(tmap: TinyMap): F[TinyMapId] = {
    val id = TinyMapId.fromUUID(UUID.randomUUID())
    tmaps = tmaps.addOne(id -> tmap)
    id.pure[F]
  }
}

object TinyMapsRepositoryDB {

  def apply[F[_]: Sync](): F[TinyMapsRepositoryDB[F]] = new TinyMapsRepositoryDB[F].pure
}
