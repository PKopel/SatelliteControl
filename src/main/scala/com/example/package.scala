package com

package object example {
  type QueryID = Int
  type SatelliteID = Int

  sealed class Message

  case class Status(queryID: QueryID, satelliteID: SatelliteID, status: SatelliteAPI.Status) extends Message {
    override def toString: String = s"$satelliteID: $status"
  }

  case class Request(queryId: QueryID, firstSatId: SatelliteID, range: Int, timeout: Int) extends Message

  case class Response(queryId: QueryID, status: List[Status], percentage: Int) extends Message
}
