package com

package object example {
  type QueryId = Int

  case class Request(queryId: QueryId, firstSatId: Int, range: Int, timeout: Int)

  case class Response(queryId: QueryId, status: Map[Int, SatelliteAPI.Status], percentage: Int)
}
