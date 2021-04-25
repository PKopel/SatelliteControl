package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout

import java.util.concurrent.TimeUnit
import scala.collection.mutable.ListBuffer
import scala.util.Success

object Dispatcher {
  def apply(): Behavior[Message] =
    Behaviors.setup(context => new Dispatcher(context))
}

class Dispatcher(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  var satellites: Map[SatelliteID, ActorRef[Request]] = Map()
  var queries: Map[QueryID, (ActorRef[Response], Int, Int, ListBuffer[Status])] = Map()

  override def onMessage(msg: Message): Behavior[Message] =
    msg match {
      case status@Status(queryID, _, _) =>
        val (stationRef, number, numberLeft, oldList) = queries(queryID)
        val newList = oldList += status
        queries += (queryID -> (stationRef, numberLeft - 1, newList))
        if (numberLeft -1 == 0) stationRef ! Response(queryID, newList.toList, numberLeft)
        this
      case Request(queryId, firstSatId, range, timeoutLength) =>
        implicit val timeout: Timeout = Timeout(timeoutLength, TimeUnit.MILLISECONDS)
        val responses = ListBuffer[Status]()
        for (sat <- firstSatId to firstSatId + range) {
          satellites.get(sat).foreach(ref => context.ask(ref, Satellite.Request(queryId, sat, _: ActorRef[Message])) {
            case Success(value) => value
          })
        }
        this
    }
}
