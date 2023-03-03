package coding.tiny.map

import cats.effect.{ExitCode, IO, IOApp}
import dev.profunktor.redis4cats.effect.Log.Stdout.instance

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

    TinyMapServer.stream[IO].flatMap(_.compile.drain.as(ExitCode.Success))
  }

}
