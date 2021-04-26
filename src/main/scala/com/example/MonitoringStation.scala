package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.example.SatelliteAPI.{NAVIGATION_ERROR, PROPULSION_ERROR}
import slick.jdbc.SQLiteProfile
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.ExecutionContext
import scala.util.Random

object MonitoringStation {
  def apply(name: String)
           (implicit db: SQLiteProfile.backend.Database,
            dispatcherRef: ActorRef[Message],
            ec: ExecutionContext): Behavior[Message] =
    Behaviors.setup { context => new MonitoringStation(name, context) }
}

class MonitoringStation(val name: String,
                        context: ActorContext[Message])
                       (implicit db: SQLiteProfile.backend.Database,
                        dispatcherRef: ActorRef[Message],
                        ec: ExecutionContext)
  extends AbstractBehavior[Message](context) {
  context.log.info(s"Station $name started")

  var queryNumber = 0
  val rand = new Random()
  var queries: Map[QueryID, Long] = Map()

  override def onMessage(msg: Message): Behavior[Message] =
    msg match {
      case _: SendToSat => sendToSat()
      case SendToDB(satelliteID) => sendToDB(satelliteID)
      case DBResponse(satelliteID, errors) => logDBResponse(satelliteID, errors)
      case Response(queryId, statusMap, percentage) =>
        val errors = statusMap.filter { stat =>
          stat.status == NAVIGATION_ERROR || stat.status == PROPULSION_ERROR
        }
        logResponse(queryId, errors, percentage)
        saveResponse(errors)
      case _ => this
    }

  def sendToSat(): MonitoringStation = {
    val queryID = (context.self, queryNumber)
    val firstSatID = 100 + rand.nextInt(50)
    queries += (queryID -> System.currentTimeMillis())
    dispatcherRef ! Request(queryID, firstSatID, range = 50, timeout = 300)
    queryNumber += 1
    this
  }

  def sendToDB(satelliteID: SatelliteID): MonitoringStation = {
    dispatcherRef ! DBRequest(context.self, satelliteID)
    this
  }

  def logDBResponse(satelliteID: SatelliteID, errors: Int): MonitoringStation = {
    if (errors > 0) {
      context.log.info(s"Station: $name")
      println(s"number of errors from satellite $satelliteID: $errors")
    }
    this
  }

  def logResponse(queryId: QueryID, errors: List[Status], percentage: Double): MonitoringStation = {
    val time = System.currentTimeMillis() - queries(queryId)
    val errorNumber = errors.size
    val stringBuilder = new StringBuilder(
      s"""response time: $time
responses from $percentage% of satellites
number of errors: $errorNumber\n""")
    errors.foreach { stat => stringBuilder.append(s"$stat\n") }
    context.log.info(s"Station: $name")
    println(stringBuilder.toString())
    this
  }

  def saveResponse(errors: List[Status])(implicit ec: ExecutionContext): MonitoringStation = {
    val statTable = TableQuery[StatTable]
    val read = statTable.map(_.numberOfErrors).result
    db.run(read).andThen { results =>
      val values = results.get
      val update = DBIO.sequence(
        for (stat <- errors) yield
          statTable += (stat.satelliteID, values(stat.satelliteID - 100) + 1)
      )
      db.run(update)
    }
    this
  }
}
