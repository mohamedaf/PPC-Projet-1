package upmc.akka.culto

import akka.actor.{Actor}

class Election(id:Int) extends Actor {

  abstract class State
  case object Passive extends State
  case object Candidat extends State
  case object Dummy extends State
  case object Waiting extends State
  case object Leader extends State

  var status:State = Passive
  var cand_pred:Int = -1
  var cand_succ:Int = -1

  val father = context.actorSelection ("../")

  def receive = {

    case "INIT" => {

      status = Candidat
      cand_pred = -1
      cand_succ = -1
      val index = neighborIndex()
      val r = context.actorSelection("akka.tcp://System" + nod.nodes(index).id + "@" + nod.nodes(index).hostname + ":" + nod.nodes(index).port + "/user/node/election")
      Thread.sleep(2000)
      r ! ALG(id)

    }

    case ALG(j) => {

      if (status == Passive) {
        status = Dummy
        val index = neighborIndex()
        val r = context.actorSelection("akka.tcp://System" + nod.nodes(index).id + "@" + nod.nodes(index).hostname + ":" + nod.nodes(index).port + "/user/node/election")
        r ! ALG(j)
      }
      else if (status == Candidat) {
        cand_pred = j

        if (id > j) {
          if (cand_succ == -1) {
            status = Waiting
            val r = context.actorSelection("akka.tcp://System" + nod.nodes(j).id + "@" + nod.nodes(j).hostname + ":" + nod.nodes(j).port + "/user/node/election")
            r ! AVS(id)
          }
          else {
            val r = context.actorSelection("akka.tcp://System" + nod.nodes(cand_succ).id + "@" + nod.nodes(cand_succ).hostname + ":" + nod.nodes(cand_succ).port + "/user/node/election")
            r ! AVSRSP(cand_pred)
            status = Dummy
          }
        } else if (id == j) {
          status = Leader
          father ! LeaderChanged(id)
        }
      }
    }

    case AVS(j) => {

      if (status == Candidat) {
        if (cand_pred == -1) {
          cand_succ = j
        }
        else {
          val r = context.actorSelection("akka.tcp://System" + nod.nodes(j).id + "@" + nod.nodes(j).hostname + ":" + nod.nodes(j).port + "/user/node/election")
          r ! AVSRSP(cand_pred)
          status = Dummy
        }
      }
      else if (status == Waiting) {
        cand_succ = j
      }
    }

    case AVSRSP(j) => {

      if (status == Waiting) {
        if (id == j) {
          status = Leader
          father ! LeaderChanged(id)
        }
        else {
          cand_pred = j

          if (cand_succ == -1) {
            if (j < id) {
              status = Waiting
              val r = context.actorSelection("akka.tcp://System" + nod.nodes(j).id + "@" + nod.nodes(j).hostname + ":" + nod.nodes(j).port + "/user/node/election")
              r ! AVS(id)
            }
          }
          else {
            status = Dummy
            val r = context.actorSelection("akka.tcp://System" + nod.nodes(cand_succ).id + "@" + nod.nodes(cand_succ).hostname + ":" + nod.nodes(cand_succ).port + "/user/node/election")
            r ! AVSRSP(j)
          }
        }
      }
    }

  }

  def neighborIndex() : Int = {
    var index = -1
    if(nod.aliveNodes.size == 1) {
      index = nod.aliveNodes(0)
    } else {
      for(i <- 0 to (nod.aliveNodes.size - 1)) {
        if(nod.aliveNodes(i) == id) {
          if(i == (nod.aliveNodes.size - 1)) {
            index = nod.aliveNodes(0)
          } else {
            index = nod.aliveNodes(i+1)
          }
        }
      }
    }
    index
  }

}
