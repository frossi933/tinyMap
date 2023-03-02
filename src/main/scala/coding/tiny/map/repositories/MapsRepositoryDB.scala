package coding.tiny.map.repositories

import cats.Applicative
import cats.effect.Sync
import cats.implicits.catsSyntaxApplicativeId
import coding.tiny.map.model.tinyMap.{TinyMap, TinyMapName}
import eu.timepit.refined.types.all.NonEmptyString

import scala.collection.mutable

class MapsRepositoryDB[F[_]: Applicative] extends MapsRepository[F] {
  var tmaps: mutable.HashMap[TinyMapName, TinyMap] = mutable.HashMap[TinyMapName, TinyMap]()

  override def getAll(): F[List[TinyMap]] = tmaps.toList.map(_._2).pure[F]

  override def getByName(name: TinyMapName): F[TinyMap] = tmaps(name).pure[F]

  override def save(tmap: TinyMap): F[TinyMapName] = {
    val name = TinyMapName(NonEmptyString.unsafeFrom("map-" + tmaps.size))
    tmaps = tmaps.addOne(name -> tmap)
    name.pure[F]
  }
}

object MapsRepositoryDB {

  def apply[F[_]: Sync](): F[MapsRepositoryDB[F]] = new MapsRepositoryDB[F].pure
}
