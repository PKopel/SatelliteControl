import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, DispatcherSelector}
import com.example._
import com.typesafe.config.ConfigFactory
import slick.jdbc.SQLiteProfile
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.ExecutionContext

object Main extends App {

  val config = ConfigFactory.load()
  implicit val db: SQLiteProfile.backend.Database = Database.forConfig("satellite")

  def startDB() {
    val statTable = TableQuery[StatTable]
    val setup = DBIO.seq(
      statTable.schema.create
    )
    val insert = DBIO.sequence(
      for (satID <- 100 until 200) yield statTable += (satID, 0)
    )
    db.run(setup)
    db.run(insert)
  }

  def startActorSystem() {
    val systemGuardian = Behaviors.setup((context: ActorContext[Unit]) => {
      implicit val executionContext: ExecutionContext =
        context.system.dispatchers.lookup(DispatcherSelector.fromConfig("my-dispatcher"))
      implicit val dispatcher: ActorRef[Message] = context.spawn(Dispatcher(), "dispatcher")
      var stations = Map[String, ActorRef[Message]]()
      List("station1", "station2", "station3").foreach(
        name => stations += (name -> context.spawn(MonitoringStation(name), name))
      )
      stations.foreach { case (_, ref) =>
        ref ! new SendToSat()
        ref ! new SendToSat()
      }

      try Thread.sleep(1000)
      catch {
        case e: InterruptedException =>
          e.printStackTrace()
      }
      for (id <- 0 until 100) {
        println("sending db requests")
        stations.head._2 ! SendToDB(100 + id)
      }

      Behaviors
        .receive[Unit]((_, _) => Behaviors.same)
    })
    ActorSystem(systemGuardian, "constellation", config)
  }

  startDB()
  startActorSystem()
}
