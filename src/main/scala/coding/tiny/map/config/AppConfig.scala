package coding.tiny.map.config

final case class RedisConfig(uri: String)

final case class BlazeServerConfig(host: String, port: Int)

final case class AppConfig(
    redis: RedisConfig,
    blaze: BlazeServerConfig
)

object AppConfig {

  def mkDefault: AppConfig =
    AppConfig(RedisConfig("redis://localhost"), BlazeServerConfig("0.0.0.0", 8080))
}
