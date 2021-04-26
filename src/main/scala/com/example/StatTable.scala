package com.example

import slick.jdbc.SQLiteProfile.api._
import slick.lifted.ProvenShape

class StatTable(tag: Tag) extends Table[(Int, Int)](tag, "SatelliteStat") {
  def satID: Rep[Int] = column[Int]("sat_ID", O.PrimaryKey)

  def numberOfErrors: Rep[Int] = column[Int]("number_of_errors")

  def * : ProvenShape[(SatelliteID, SatelliteID)] = (satID, numberOfErrors)
}
