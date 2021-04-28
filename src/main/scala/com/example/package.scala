package com

import akka.actor.typed.ActorRef

package object example {
  type QueryID = (ActorRef[Response], Int)
  type SatelliteID = Int

  sealed class Message

  class SendToSat extends Message

  case class SendToDB(satelliteID: SatelliteID) extends Message

  case class Status(queryID: QueryID, satelliteID: SatelliteID, status: SatelliteAPI.Status) extends Message {
    override def toString: String = s"$satelliteID: $status"
  }

  case class Request(queryID: QueryID, firstSatID: SatelliteID, range: Int, timeout: Long) extends Message

  case class DBRequest(sender: ActorRef[Message], satelliteID: SatelliteID) extends Message

  case class Response(queryID: QueryID, status: List[Status], percentage: Double) extends Message

  case class DBResponse(satelliteID: SatelliteID, errors: Int) extends Message
}
