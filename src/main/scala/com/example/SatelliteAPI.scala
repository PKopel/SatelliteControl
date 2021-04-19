package com.example

import scala.util.Random

object SatelliteAPI extends Enumeration {
  object Status {
    type Status = Value
    val OK, BATTERY_LOW, PROPULSION_ERROR, NAVIGATION_ERROR = Value
  }

  def getStatus(satelliteIndex: Int): SatelliteAPI.Status.Status = {
    val rand = new Random()
    try Thread.sleep(100 + rand.nextInt(400))
    catch {
      case e: InterruptedException =>
        e.printStackTrace()
    }
    val p = rand.nextDouble
    if (p < 0.8) return Status.OK
    else if (p < 0.9) return Status.BATTERY_LOW
    else if (p < 0.95) return Status.NAVIGATION_ERROR
    Status.PROPULSION_ERROR
  }
}
