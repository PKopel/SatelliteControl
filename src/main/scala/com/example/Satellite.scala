package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.{ExecutionContext, Future}

object Satellite {
  case class Request(queryID: QueryID, satelliteID: SatelliteID, replyTo: ActorRef[Message])

  def apply()(implicit ec: ExecutionContext): Behavior[Request] =
    Behaviors.setup { _ =>
      Behaviors.receiveMessage[Request] {
        request: Request =>
          reportStatus(request)
          Behaviors.same
      }
    }

  def reportStatus(request: Request)(implicit ec: ExecutionContext): Unit = {
    Future {
      val status = SatelliteAPI.getStatus(request.satelliteID)
      request.replyTo ! Status(request.queryID, request.satelliteID, status)
    }
  }
}

