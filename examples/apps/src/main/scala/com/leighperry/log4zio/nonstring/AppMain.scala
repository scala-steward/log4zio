package com.leighperry.log4zio.nonstring

import com.leighperry.log4zio.Log.SafeLog
import com.leighperry.log4zio.{Log, LogMedium, RawLogMedium, Tagged}
import zio.{ExitCode, IO, UIO, ZIO}

object AppMain extends zio.App {

  def intLogger: UIO[SafeLog[Int]] =
    Log.make[Nothing, Int](intRendered(RawLogMedium.console))

  def intRendered(base: LogMedium[Nothing, String]): LogMedium[Nothing, Tagged[Int]] =
    base.contramap {
      m: Tagged[Int] =>
        val n: Int = m.message()
        "%-5s - %d:%s".format(m.level.name, n, "x" * n)
    }

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    for {
      log <- intLogger

      pgm = new Application(log).execute

      exitCode <- pgm *> log.info(10) *> IO.succeed(ExitCode.success)
    } yield exitCode
}

// The core application
class Application(log: SafeLog[Int]) {
  val doSomething: IO[Nothing, Unit] =
    for {
      _ <- log.info(1)
      _ <- log.info(2)
    } yield ()

  val execute: IO[Nothing, Unit] =
    for {
      _ <- log.info(3)
      _ <- doSomething
      _ <- log.info(4)
    } yield ()
}
