package upmc.akka.culto

import akka.actor.{Props, Actor}

class Node(id:Int, nodes:List[System]) extends Actor {

    val electionActor = context.actorOf(Props(new Election(id)), name = "election")
    val checkerActor = context.actorOf(Props(new Checker(id)), name = "checker")
    val beatActor = context.actorOf(Props(new Beat(id)), name = "beat")
    val displayActor = context.actorOf(Props[Display], name = "display")

    def receive = {

      case "START" => {

        // notify other nodes
        nod.nodes.foreach(n => {
          val r = context.actorSelection("akka.tcp://System" + n.id + "@" + n.hostname + ":" + n.port + "/user/node")
          r ! New(id)
        })

      }

      case "Elector" => {

        electionActor ! "INIT"

      }

      case New(nodeId) => {

        self ! OutBeatMessage(nodeId, BeatM(nodeId))
        nod.aliveNodes = nod.aliveNodes ::: List(nodeId)
        nod.nbNodes = nod.nbNodes + 1
        displayActor ! print("New node connected, id : " + nodeId +", nb nodes " + nod.nbNodes)
        displayActor ! print("List of nodes : " + nod.aliveNodes.toString())

        if(nodeId != id) {
          val r = context.actorSelection("akka.tcp://System" + nodeId + "@" + nod.nodes(nodeId).hostname + ":" + nod.nodes(nodeId).port + "/user/node")
          r ! Ok(id, nod.leader_id)
        }

      }

      case Ok(nodeId, leader_id) => {

        self ! OutBeatMessage(nodeId, BeatM(nodeId))
        nod.aliveNodes = nod.aliveNodes ::: List(nodeId)
        nod.nbNodes = nod.nbNodes + 1
        nod.leader_id = leader_id
        displayActor ! print("Ok call from node " + nodeId + ", nb nodes " + nod.nbNodes + ", leader_id " + nod.leader_id)
        displayActor ! print("Leader is " + nod.leader_id)
        displayActor ! print("List of nodes : " + nod.aliveNodes.toString())

      }

      case OutBeatMessage(nodeId, beatMessage) => {

        if(nodeId == id) {
          // notify other nodes checkers
          nod.nodes.foreach(n => {
            if(n.id != id && nod.aliveNodes.contains(n.id)) {
              val r = context.actorSelection("akka.tcp://System" + n.id + "@" + n.hostname + ":" + n.port + "/user/node")
              r ! OutBeatMessage(nodeId, beatMessage)
            }
          })
        }

        beatMessage match {
          case BeatM(id) => {
            checkerActor ! BeatM(id)
          }
          case LeaderBeatM(id) => {
            checkerActor ! LeaderBeatM(id)
          }
        }

      }

      case Die(nodeId) => {

        // remove value
        var tmpAliveNodes:List[Int] = List()
        nod.aliveNodes.foreach( i => {
          if(i != nodeId) {
            tmpAliveNodes = tmpAliveNodes ::: List(i)
          }
        })
        nod.aliveNodes = List() ::: tmpAliveNodes
        nod.nbNodes = nod.nbNodes - 1
        if(nodeId == nod.leader_id) {
          electionActor ! "INIT"
        }
        displayActor ! print("Node " + nodeId + " is die")
        displayActor ! print(nod.aliveNodes.toString())

      }

      case LeaderChanged(nodeId) => {

        if(nodeId == id) {
          nod.nodes.foreach(n => {
            if(n.id != id && nod.aliveNodes.contains(n.id)) {
              val r = context.actorSelection("akka.tcp://System" + n.id + "@" + n.hostname + ":" + n.port + "/user/node")
              r ! LeaderChanged(nodeId)
            }
          })
        }
        nod.leader_id = nodeId
        displayActor ! print("Leader changed, new Leader " + nod.leader_id)

      }

      case print(message:String) => {

        displayActor ! print(message:String)

      }

    }

}
