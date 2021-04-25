package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import com.example.SatelliteAPI.{NAVIGATION_ERROR, PROPULSION_ERROR}

import scala.util.Random

object MonitoringStation {
  def apply(name: String, dispatcherRef: ActorRef[Request]): Behavior[Message] =
    Behaviors.setup { context => new MonitoringStation(name, dispatcherRef, context) }
}

class MonitoringStation(val name: String,
                        val dispatcherRef: ActorRef[Request],
                        context: ActorContext[Message])
  extends AbstractBehavior[Message](context) {
  context.log.info(s"Station $name started")

  var queryNumber = 0
  val rand = new Random()
  var queries: Map[QueryID, Long] = Map()

  override def onMessage(msg: Message): Behavior[Message] =
    msg match {
      case _: Send =>
        val queryID = (context.self, queryNumber)
        val firstSatID = 100 + rand.nextInt(50)
        queries += (queryID -> System.currentTimeMillis())
        dispatcherRef ! Request(queryID, firstSatID, range = 50, timeout = 300)
        queryNumber += 1
        this
      case Response(queryId, statusMap, percentage) =>
        val time = System.currentTimeMillis() - queries(queryId)
        val errors = statusMap.filter { stat => stat.status == NAVIGATION_ERROR && stat.status == PROPULSION_ERROR }
        val errorNumber = errors.size
        context.log.info(s"Station: $name")
        println(s"""response time: $time
responses from $percentage% of satellites
number of errors: $errorNumber""")
        errors.foreach { stat => println(stat) }
        this
      case _ => this
    }
}
