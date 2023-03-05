package coding.tiny.map.resources

import cats.effect.{Concurrent, ContextShift, Resource}
import coding.tiny.map.collections.UndiGraphHMap
import coding.tiny.map.config.{AppConfig, BlazeServerConfig}
import coding.tiny.map.model.tinyMap.{City, Distance, TinyMap, TinyMapId}
import dev.profunktor.redis4cats.effect.{Log => R4CLogger}
import dev.profunktor.redis4cats.{Redis, RedisCommands}

sealed abstract class AppResources[F[_]](
    val redis: RedisCommands[F, TinyMapId, UndiGraphHMap[City, Distance]],
    val blaze: BlazeServerConfig
)

object AppResources {

  def make[F[_]: Concurrent: R4CLogger: ContextShift](
      cfg: AppConfig
  ): Resource[F, AppResources[F]] = {
    Redis[F]
      .simple(cfg.redis.uri, TinyMap.redisJsonCodec.redisCodec)
      .map { tinyMapRedisCmd =>
        new AppResources[F](tinyMapRedisCmd, cfg.blaze) {}
      }
  }
}
