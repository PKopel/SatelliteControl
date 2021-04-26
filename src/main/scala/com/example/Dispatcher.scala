package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout

import java.util.concurrent.TimeUnit
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success}

object Dispatcher {
  def apply(): Behavior[Message] =
    Behaviors.setup { context =>
      new Dispatcher(context)
    }
}

class Dispatcher(context: ActorContext[Message]) extends AbstractBehavior[Message](context) {
  context.log.info(s"Dispatcher started")
  type QueryStatus = (Int, Int, Int, ListBuffer[Status])
  var satellites: Map[SatelliteID, ActorRef[Satellite.Request]] =
    (100 until 200).map(id => (id, context.spawn(Satellite(), s"satellite$id"))).toMap
  var queries: Map[QueryID, QueryStatus] = Map()

  override def onMessage(msg: Message): Behavior[Message] =
    msg match {
      case status@Status(queryID@(ref, _), _, value) =>
        val (total, numberOk, numberTimeout, oldList) = queries(queryID)
        val newList = oldList += status
        if (value == null)
          queries += (queryID -> (total, numberOk, numberTimeout + 1, newList))
        else
          queries += (queryID -> (total, numberOk + 1, numberTimeout, newList))

        if (numberOk + numberTimeout == total) {
          val statusMap = newList.map { status => (status.satelliteID, status.status) }.toMap
          ref ! Response(queryID, statusMap, numberOk * 100.0 / total)
          queries -= queryID
        }
        this
      case Request(queryId, firstSatId, range, timeoutLength) =>
        implicit val timeout: Timeout = Timeout(timeoutLength, TimeUnit.MILLISECONDS)
        queries += (queryId -> (range, 0, 0, ListBuffer[Status]()))
        for (sat <- firstSatId to firstSatId + range) {
          satellites.get(sat).foreach(ref => context.ask(ref, Satellite.Request(queryId, sat, _: ActorRef[Message])) {
            case Success(value) => value
            case Failure(_) => Status(queryId, sat, null)
          })
        }
        this
      case _ => this
    }
}
