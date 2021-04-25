import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, DispatcherSelector, Terminated}
import com.example.{Dispatcher, Message, MonitoringStation, Send}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

object Main extends App {
  println("Loading config")
  var stations = Map[String, ActorRef[Message]]()
  val config = ConfigFactory.load()
  println(DispatcherSelector.fromConfig("my-dispatcher"))
  val systemGuardian = Behaviors.setup((context: ActorContext[Unit]) => {
    //implicit val executionContext: ExecutionContext =
    //  context.system.dispatchers.lookup(DispatcherSelector.fromConfig("my-dispatcher"))
    val dispatcher = context.spawn(Dispatcher(), "dispatcher")
    var stations = Map[String, ActorRef[Message]]()
    List("station1", "station2", "station3").foreach(
      name => stations += (name -> context.spawn(MonitoringStation(name, dispatcher), name))
    )
    stations.foreach { case (name, ref) =>
      println(s"activating station $name")
      ref ! new Send()
    }
    Behaviors
      .receive[Unit]((_, _) => Behaviors.same)
  })
  println("Starting system")
  val satelliteConstellation = ActorSystem(systemGuardian, "constellation", config)
}
