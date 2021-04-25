import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem}
import com.example.{Dispatcher, Message, MonitoringStation, Send}
import com.typesafe.config.ConfigFactory

object Main extends App {
  var stations = Map[String, ActorRef[Message]]()
  val config = ConfigFactory.load()
  val systemGuardian = Behaviors.setup((context: ActorContext[Unit]) => {
    val dispatcher = context.spawn(Dispatcher(), "dispatcher")
    var stations = Map[String, ActorRef[Message]]()
    List("station1", "station2", "station3").foreach(
      name => stations += (name -> context.spawn(MonitoringStation(name, dispatcher), name))
    )
    stations.foreach { case (_, ref) =>
      ref ! new Send()
      ref ! new Send()
    }
    Behaviors
      .receive[Unit]((_, _) => Behaviors.same)
  })
  val satelliteConstellation = ActorSystem(systemGuardian, "constellation", config)
}
