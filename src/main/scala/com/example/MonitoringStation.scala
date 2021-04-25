package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.example.SatelliteAPI.{NAVIGATION_ERROR, PROPULSION_ERROR}

import scala.util.Random

object MonitoringStation {
  def apply(name: String, dispatcherRef: ActorRef[Request]): Behavior[Response] =
    Behaviors.setup(context => new MonitoringStation(name, dispatcherRef, context))
}

class MonitoringStation(val name: String,
                        val dispatcherRef: ActorRef[Request],
                        context: ActorContext[Response])
  extends AbstractBehavior[Response](context) {
  context.log.info(s"Station $name started")

  val queries: Map[QueryID, Int] = Map()

  override def onMessage(msg: Response): Behavior[Response] =
    msg match {
      case Response(queryId, statusMap, _) =>
        val time = System.currentTimeMillis() - queries.get(queryId)
        val errors = statusMap.filter { stat => stat.status == NAVIGATION_ERROR && stat.status == PROPULSION_ERROR }
        val errorNumber = errors.size
        println(s"station: $name")
        println(s"response time: $time")
        println(s"number of errors: $errorNumber")
        errors.foreach { stat => println(stat) }
        this
    }

  def run(): Unit = {
    val rand = new Random()
    dispatcherRef ! Request(queryId = 1, firstSatId = 100 + rand.nextInt(50), range = 50, timeout = 300)
    dispatcherRef ! Request(queryId = 2, firstSatId = 100 + rand.nextInt(50), range = 50, timeout = 300)
  }
}
