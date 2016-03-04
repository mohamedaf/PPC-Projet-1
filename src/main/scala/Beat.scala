package upmc.akka.culto

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

import akka.actor.{Actor}

class Beat(id:Int) extends Actor {

    val system = context.system
    val scheduler = context.system.scheduler
    val father = context.actorSelection ("../")

    case object Tick

    def receive = {

        case Tick => {

          if (id != nod.leader_id) {
            father ! OutBeatMessage(id, BeatM(id))
          } else {
            father ! OutBeatMessage(id, LeaderBeatM(id))
          }
          scheduler.scheduleOnce(100 milliseconds, self, Tick)

        }
        case _ =>
    }

    self ! Tick
}
