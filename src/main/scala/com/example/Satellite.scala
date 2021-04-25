package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object Satellite {
  case class Request(queryID: QueryID, satelliteID: SatelliteID, replyTo: ActorRef[Message])

  def apply(): Behavior[Request] =
    Behaviors.receiveMessage[Request] {
      case Request(queryID, satelliteID, replyTo) =>
        val status = SatelliteAPI.getStatus(satelliteID)
        replyTo ! Status(queryID, satelliteID, status)
        Behaviors.same
    }
}

