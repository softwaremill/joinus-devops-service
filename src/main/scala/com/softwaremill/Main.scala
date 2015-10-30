package com.softwaremill

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.language.postfixOps
import scala.util.Try
import scala.sys.process.stringToProcess

object Main extends App with StrictLogging {
  implicit val as = ActorSystem()

  import as.dispatcher

  implicit val mat = ActorMaterializer()

  var isOk = false

  val startTime = System.currentTimeMillis()

  def uptime = Duration(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS).toSeconds

  val routes = get {
    path("status") {
      if (isOk) {
        logger.info("Status OK")
        complete( s"""{"status": "OK", "uptime": $uptime}""")
      } else {
        logger.error("Error")
        complete(500, "ERROR")
      }
    }
  }

  startUp()

  private def startUp() = {
    Future.sequence(List(
      bindPort,
      checkInternetOut,
      checkTooling,
      checkRam,
      checkStorage
    ))
      .transform(_ => {
        logger.info("SUCCESS: Server fully initialized")
        isOk = true
      },
        e => {
          logger.error(s"FAILURE: ${e.getMessage}")
          e
        })
  }

  private def bindPort(): Future[Unit] = {
    Http().bindAndHandle(routes, "0.0.0.0", 8081).transform(_ => {
      logger.info("Server bound to port"); ();
    }, e => exc(s"Cannot start server $e"))
  }

  private def checkInternetOut(): Future[Unit] = {
    val error = exc("No internet connection or faulty connection detected.")

    Http().singleRequest(HttpRequest(uri = "http://example.org")).flatMap(s =>
      if (s.status.isFailure()) {
        Future.failed(error)
      }
      else {
        Future.successful(())
      }).transform(identity, _ => error)
  }

  private def checkTooling(): Future[Unit] = Future.fromTry(Try {

    val dependency = "cowsay"
    val exitCode = stringToProcess(s"$dependency 'there is no cow level'").!
    if (exitCode > 0) {
      throw exc(s"External dependency '$dependency' return exit code $exitCode, please check command availability.")
    }
    ()
  })

  private def checkRam(): Future[Unit] = Future.fromTry(Try {
    def toGb(bytes: Long) = bytes.toDouble / Math.pow(1024.0, 3)
    val totalMem = toGb(Runtime.getRuntime.maxMemory())
    if (totalMem < 2.0) {
      throw exc(s"Total JVM available memory less than 2GB. Please check system configuration and/or Java Virtual Machine maximum heap settings. Memory detected: $totalMem GB.")
    }
    ()
  })

  private def checkStorage(): Future[Unit] = Future.fromTry(Try {
    val storageReqsInMegs = Map(
      "/vol/db" -> 1024,
      "." -> 100
    )

    def calcSpaceInMegs(pathString: String) = {
      stringToProcess(s"df -m $pathString --output=avail").!!.split("\n").last.trim.toInt
    }

    storageReqsInMegs.map { case (path, req) =>
      val space = calcSpaceInMegs(path)
      path ->(req, space >= req)
    }.find(!_._2._2).foreach { case (path, reqStatus) => throw exc(s"Insufficient storage in path '$path', required amount ${reqStatus._1} MB") }
    ()
  })


  private def exc(msg: String) = new InitException(msg)

}

class InitException(msg: String) extends RuntimeException(msg)