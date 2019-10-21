package com.leighperry.log4zio.nonstring

import com.leighperry.log4zio.{ Log, LogMedium, RawLogMedium, Tagged }
import zio.{ UIO, ZIO }

object AppMain extends zio.App {

  def intLogger: UIO[Log[Int]] = {
    val value: LogMedium[Tagged[Int]] = intRendered(RawLogMedium.console)
    Log.make[Int](value)
  }

  def intRendered(base: LogMedium[String]): LogMedium[Tagged[Int]] =
    base.contramap {
      m: Tagged[Int] =>
        val n: Int = m.message()
        "%-5s - %d:%s".format(m.level.name, n, "x" * n)
    }

  final case class AppEnv(log: Log.Service[Int]) extends Log[Int]

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    for {
      logsvc <- intLogger
      log = logsvc.log

      pgm = for {
        _ <- Application.execute.provide(AppEnv(log))
      } yield ()

      exitCode <- pgm.foldM(
        e => log.error(11) *> ZIO.succeed(1),
        _ => log.info(10) *> ZIO.succeed(0)
      )
    } yield exitCode
}

// The core application
object Application {
  val doSomething: ZIO[Log[Int], Nothing, Unit] =
    for {
      log <- Log.log[Int]
      _ <- log.info(1)
      _ <- log.info(2)
    } yield ()

  val execute: ZIO[Log[Int], Nothing, Unit] =
    for {
      log <- Log.log[Int]
      _ <- log.info(3)
      _ <- doSomething
      _ <- log.info(4)
    } yield ()
}
