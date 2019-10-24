package com.leighperry.log4zio.loggingonly

import com.leighperry.log4zio.{Log, LogE}
import zio.ZIO

object AppMain extends zio.App {

  final case class AppEnv(log: LogE.Service[Nothing, String]) extends Log[String]

  val appName = "logging-app"

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    for {
      logsvc <- LogE.safeConsole[String](Some(appName))
      log = logsvc.log

      pgm = Application.execute.provide(AppEnv(log))

      exitCode <- pgm *> log.info("Application terminated with no error indication") *> ZIO.succeed(0)
    } yield exitCode
}

// The core application
object Application {
  val doSomething: ZIO[Log[String], Nothing, Unit] =
    for {
      log <- LogE.stringLog
      _ <- log.info(s"Executing something")
      _ <- log.info(s"Finished executing something")
    } yield ()

  val execute: ZIO[Log[String], Nothing, Unit] =
    for {
      log <- LogE.stringLog
      _ <- log.info(s"Starting app")
      _ <- doSomething
      _ <- log.info(s"Finished app")
    } yield ()
}
