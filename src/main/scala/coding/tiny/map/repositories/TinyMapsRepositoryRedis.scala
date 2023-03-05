package coding.tiny.map.repositories

import cats.effect._
import cats.implicits._
import coding.tiny.map.collections.UndiGraphHMap
import coding.tiny.map.model.tinyMap.{City, Distance, TinyMap, TinyMapId}
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.effect.{Log => R4CLogger}

class TinyMapsRepositoryRedis[F[_]: Sync](
    redisCmd: RedisCommands[F, TinyMapId, UndiGraphHMap[City, Distance]]
) extends TinyMapsRepository[F] {

  override def save(mapGraph: UndiGraphHMap[City, Distance]): F[TinyMap] = for {
    id <- TinyMapId.make.pure[F]
    _  <- redisCmd.setNx(id, mapGraph)
  } yield TinyMap(id, mapGraph)

  override def update(tmap: TinyMap): F[TinyMap] = redisCmd.set(tmap.id, tmap.graph) *> tmap.pure[F]

  override def getAll: F[Vector[TinyMap]] = for {
    allEntries <- TinyMapId.allUuids.pure[F]
    ids        <- redisCmd.keys(allEntries)
    graphs     <- ids.map(id => redisCmd.get(id)).sequence
    maps        =
      ids.toVector.zip(graphs).flatMap { case (id, maybeGraph) =>
        maybeGraph.map(TinyMap(id, _)).toVector
      }
  } yield maps

  override def getById(id: TinyMapId): F[Option[TinyMap]] =
    redisCmd.get(id).map { maybeGraph =>
      maybeGraph.map(TinyMap(id, _))
    }
}

object TinyMapsRepositoryRedis {

  def make[F[_]: BracketThrow: Concurrent: ContextShift: R4CLogger](
      redisCommands: RedisCommands[F, TinyMapId, UndiGraphHMap[City, Distance]]
  ): F[TinyMapsRepositoryRedis[F]] = Sync[F].delay {
    new TinyMapsRepositoryRedis[F](redisCommands)
  }
}
