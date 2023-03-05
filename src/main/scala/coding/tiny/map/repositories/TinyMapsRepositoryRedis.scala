package coding.tiny.map.repositories

import cats.effect._
import cats.implicits._
import coding.tiny.map.collections.UndiGraphHMap
import coding.tiny.map.model.tinyMap.{City, Distance, TinyMap, TinyMapId}
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.effect.{Log => R4CLogger}

import java.util.UUID

class TinyMapsRepositoryRedis[F[_]](
    redisCmd: RedisCommands[F, TinyMapId, UndiGraphHMap[City, Distance]]
)(implicit
    ev: Sync[F]
) extends TinyMapsRepository[F] {

  override def save(mapGraph: UndiGraphHMap[City, Distance]): F[TinyMap] = for {
    id <- TinyMapId.fromUUID(UUID.randomUUID()).pure[F]
    _  <- redisCmd.setNx(id, mapGraph)
  } yield TinyMap(id, mapGraph)

  override def update(tmap: TinyMap): F[TinyMap] = redisCmd.set(tmap.id, tmap.graph) *> tmap.pure[F]

  override def getAll: F[List[TinyMap]] = for {
    allEntries <- TinyMapId.allUuids.pure[F]
    ids        <- redisCmd.keys(allEntries)
    graphs     <- ids.map(id => redisCmd.get(id)).sequence
    maps        =
      ids.zip(graphs).flatMap { case (id, maybeGraph) => maybeGraph.map(TinyMap(id, _)).toList }
  } yield maps

  override def getById(id: TinyMapId): F[Option[TinyMap]] =
    redisCmd.get(id).map(_.map(g => TinyMap(id, g)))
}

object TinyMapsRepositoryRedis {

  def apply[F[_]: BracketThrow: Concurrent: ContextShift: R4CLogger](
      redisCommands: RedisCommands[F, TinyMapId, UndiGraphHMap[City, Distance]]
  ): F[TinyMapsRepositoryRedis[F]] = {
    new TinyMapsRepositoryRedis[F](redisCommands).pure[F]
  }
}
