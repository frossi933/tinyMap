package coding.tiny.map

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  def run(args: List[String]) = {

    TinyMapServer.stream[IO].flatMap(_.compile.drain.as(ExitCode.Success))
  }

}
