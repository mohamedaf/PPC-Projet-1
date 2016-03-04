package upmc.akka.culto

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import akka.actor.{Actor}

class Checker(id:Int) extends Actor {

  val father = context.actorSelection ("../")
  val scheduler = context.system.scheduler
  var tmpAliveNodes:List[Int] = List()

  case object Tick

  var x = 0

  def receive = {

    case Tick => {

      scheduler.scheduleOnce(1200 milliseconds, self, Tick)
      if(nod.leader_id == -1 && x == 1) {
        father ! "Elector"
      }
      nod.aliveNodes.foreach(i => {
        if(!tmpAliveNodes.contains(i)) {
          father ! Die(i)
        }
      })
      nod.aliveNodes = List() ::: tmpAliveNodes
      tmpAliveNodes = List()
      x = x + 1

    }

    case BeatM(nodeId) => {

      if(!tmpAliveNodes.contains(nodeId)) {
        tmpAliveNodes  = tmpAliveNodes ::: List(nodeId)
      }

    }

    case LeaderBeatM(nodeId) => {

      if(!tmpAliveNodes.contains(nodeId)) {
        tmpAliveNodes  = tmpAliveNodes ::: List(nodeId)
      }

    }

  }

  tmpAliveNodes  = tmpAliveNodes ::: List(id)
  scheduler.scheduleOnce(300 milliseconds, self, Tick)

}
