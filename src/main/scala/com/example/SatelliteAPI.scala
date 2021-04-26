package com.example

import scala.util.Random

object SatelliteAPI extends Enumeration {
    type Status = Value
    val OK, BATTERY_LOW, PROPULSION_ERROR, NAVIGATION_ERROR = Value


  def getStatus(satelliteIndex: Int): SatelliteAPI.Status = {
    val rand = new Random()
    try Thread.sleep(100 + rand.nextInt(400))
    catch {
      case e: InterruptedException =>
        e.printStackTrace()
    }
    val p = rand.nextDouble
    /*
    if (p < 0.8) return OK
    if (p < 0.9) return BATTERY_LOW
    if (p < 0.95) return NAVIGATION_ERROR

     */
    PROPULSION_ERROR
  }
}
