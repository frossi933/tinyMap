package coding.tiny.map

import cats.effect.{ExitCode, IO, IOApp}
import coding.tiny.map.config.AppConfig
import coding.tiny.map.resources.AppResources
import dev.profunktor.redis4cats.effect.Log.Stdout.instance

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

    AppResources
      .make[IO](AppConfig.mkDefault)
      .use { res =>
        TinyMapServer.stream[IO](res).flatMap(_.compile.drain.as(ExitCode.Success))
      }
  }

}
