package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

object Dispatcher {
  def apply(): Behavior[Request] =
    Behaviors.setup(context => new Dispatcher(context))
}

class Dispatcher(context: ActorContext[Request]) extends AbstractBehavior[Request](context) {
  val satellites: Map[SatelliteID, ActorRef[String]] = Map()

  override def onMessage(msg: Request): Behavior[Request] =
    msg match {
      case Request(queryId, firstSatId, range, timeout) =>
        for (sat <- firstSatId to firstSatId + range) {
          satellites.get(sat).foreach(ref => ref ! "msg")
        }
        this
    }
}
