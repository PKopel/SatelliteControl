package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.util.Timeout
import slick.jdbc.SQLiteProfile
import slick.jdbc.SQLiteProfile.api._

import java.util.concurrent.TimeUnit
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Dispatcher {
  def apply()(implicit db: SQLiteProfile.backend.Database,
              ec: ExecutionContext): Behavior[Message] =
    Behaviors.setup { context =>
      new Dispatcher(context)
    }
}

class Dispatcher(context: ActorContext[Message])
                (implicit db: SQLiteProfile.backend.Database,
                 ec: ExecutionContext)
  extends AbstractBehavior[Message](context) {
  context.log.info(s"Dispatcher started")
  type QueryStatus = (Int, Int, Int, ListBuffer[Status])
  var satellites: Map[SatelliteID, ActorRef[Satellite.Request]] =
    (100 until 200).map(id => (id, context.spawn(Satellite(), s"satellite$id"))).toMap
  var queries: Map[QueryID, QueryStatus] = Map()

  override def onMessage(msg: Message): Behavior[Message] =
    msg match {
      case status@Status(queryID@(ref, _), _, _) =>
        processStatus(queryID, ref, status)
      case Request(queryId, firstSatId, range, timeoutLength) =>
        processRequest(queryId, firstSatId, range, timeoutLength)
      case DBRequest(sender, satelliteID) =>
        processDBRequest(sender, satelliteID)
      case _ => this
    }

  def processStatus(queryID: QueryID, ref: ActorRef[Response], status: Status): Dispatcher = {
    val (total, numberOk, numberTimeout, oldList) = queries(queryID)
    val newList = oldList += status
    if (status.status == null)
      queries += (queryID -> (total, numberOk, numberTimeout + 1, newList))
    else
      queries += (queryID -> (total, numberOk + 1, numberTimeout, newList))

    if (numberOk + numberTimeout == total) {
      ref ! Response(queryID, newList.toList, numberOk * 100.0 / total)
      queries -= queryID
    }
    this
  }

  def processRequest(queryId: QueryID, firstSatId: SatelliteID, range: Int, timeoutLength: Long): Dispatcher = {
    implicit val timeout: Timeout = Timeout(timeoutLength, TimeUnit.MILLISECONDS)
    queries += (queryId -> (range, 0, 0, ListBuffer[Status]()))
    for (sat <- firstSatId to firstSatId + range) {
      satellites.get(sat).foreach(ref => context.ask(ref, Satellite.Request(queryId, sat, _: ActorRef[Message])) {
        case Success(value) => value
        case Failure(_) => Status(queryId, sat, null)
      })
    }
    this
  }

  def processDBRequest(sender: ActorRef[Message], satelliteID: SatelliteID): Dispatcher = {
    val statTable = TableQuery[StatTable]
    val read = statTable.filter(_.satID === satelliteID).map(_.numberOfErrors).result
    db.run(read).andThen(result => sender ! DBResponse(satelliteID, result.get.head))
    this
  }
}
