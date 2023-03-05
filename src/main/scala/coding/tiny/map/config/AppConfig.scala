package coding.tiny.map.config

case class RedisConfig(uri: String)

case class BlazeServerConfig(host: String, port: Int)

case class AppConfig(
    redis: RedisConfig,
    blaze: BlazeServerConfig
)

object AppConfig {

  def mkDefault: AppConfig =
    AppConfig(RedisConfig("redis://localhost"), BlazeServerConfig("0.0.0.0", 8080))
}
