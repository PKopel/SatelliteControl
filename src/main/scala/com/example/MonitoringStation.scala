package com.example

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.example.SatelliteAPI.{NAVIGATION_ERROR, PROPULSION_ERROR}

object MonitoringStation {
  def apply(name: String): Behavior[Response] =
    Behaviors.setup(context => new MonitoringStation(name, context))
}

class MonitoringStation(val name: String, context: ActorContext[Response]) extends AbstractBehavior[Response](context) {
  val queries: Map[QueryId, Int] = Map()

  override def onMessage(msg: Response): Behavior[Response] =
    msg match {
      case Response(queryId, statusMap, _) =>
        val time = System.currentTimeMillis() - queries.get(queryId)
        val errors = statusMap.filter { case (_, e) => e == NAVIGATION_ERROR && e == PROPULSION_ERROR }
        val errorNumber = errors.size
        println(s"station: $name")
        println(s"response time: $time")
        println(s"number of errors: $errorNumber")
        errors.foreach { case (id, stat) => println(s"$id: $stat") }
        this
    }
}
