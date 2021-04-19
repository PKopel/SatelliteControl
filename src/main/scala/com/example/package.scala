package com

package object example {
  type QueryID = Int
  type SatelliteID = Int

  case class Request(queryId: QueryID, firstSatId: Int, range: Int, timeout: Int)

  case class Response(queryId: QueryID, status: Map[Int, SatelliteAPI.Status], percentage: Int)
}
