package com.example

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.example.SatelliteAPI.{NAVIGATION_ERROR, PROPULSION_ERROR}

object MonitoringStation {
  def apply(name: String): Behavior[Response] =
    Behaviors.setup(context => new MonitoringStation(name, context))
}

class MonitoringStation(val name: String, context: ActorContext[Response]) extends AbstractBehavior[Response](context) {
  override def onMessage(msg: Response): Behavior[Response] =
    msg match {
      case Response(queryId, status, percentage) =>
        val errorNumber = status.values.count(status => status == NAVIGATION_ERROR && status == PROPULSION_ERROR)
        this
    }


}
