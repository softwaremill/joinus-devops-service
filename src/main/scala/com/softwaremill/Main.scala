package com.softwaremill

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.StrictLogging

import scala.util.{Failure, Properties, Success}

object Main extends App with StrictLogging {
  implicit val as = ActorSystem()
  import as.dispatcher
  implicit val mat = ActorMaterializer()

  var isOk = true

  val routes = get {
    path("status") {
      if (isOk) {
        logger.info("Status ok")
        complete("""{"status": "ok"}""")
      } else {
        logger.error("Error")
        complete(500, "ERROR")
      }
    } ~
    path("environment") {
      val environment = Properties.envOrElse("SML_ENV", "")
      logger.info(s"Environment: $environment")
      complete(s"""{"environment": "$environment"}""")
    }
  } ~ post {
    path("dowork") {
      parameter("magicnumber".as[Int]) { mn =>
        if (mn == 42) {
          sys.exit(1)
        } else {
          if (mn % 2 == 0) {
            isOk = true
            complete("done")
          } else {
            isOk = false
            complete("try again")
          }
        }
      }
    }
  }

  Http().bindAndHandle(routes, "0.0.0.0", 8081).onComplete {
    case Success(_) => logger.info("Server started")
    case Failure(f) =>
      logger.error("Cannot start server", f)
      as.shutdown()
  }
}
