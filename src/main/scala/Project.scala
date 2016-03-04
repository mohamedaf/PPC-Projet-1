package upmc.akka.culto


import com.typesafe.config.ConfigFactory

import akka.actor.{Props, ActorSystem}

abstract class BeatT
case class BeatM(id:Int) extends BeatT
case class LeaderBeatM(id:Int) extends BeatT

sealed trait Message
case class print(message:String) extends Message
case class OutBeatMessage(id:Int, beat:BeatT) extends Message
case class LeaderChanged(id:Int) extends Message
case class Die(id:Int) extends Message
case class New(id:Int) extends Message
case class Ok(id:Int, leader_id:Int) extends Message

case class ALG(id:Int) extends Message
case class AVS(id:Int) extends Message
case class AVSRSP(id:Int) extends Message

case class System(id:Int, hostname:String, port:Int)

object nod  {
  var nodes  = List(System(0, "127.0.0.1", 5000),
                    System(1, "127.0.0.1", 5001),
                    System(2, "127.0.0.1", 5002),
                    System(3, "127.0.0.1", 5003))
  var aliveNodes:List[Int] = List()
  var nbNodes:Int = 0
  var leader_id:Int = -1
}

object projet {

    def main (args : Array[String]) {

        if(args.size != 1) {
            println ("Nombre d'arguments incorrect, veuillez fournir seulement l'id du node")
            sys.exit(1)
        }

        val id : Int = args(0).toInt

        if(id < 0 || id > 3) {
            println ("Veuillez choisir un identifiant entre 0 et 3")
            sys.exit(1)
        }

        val system = ActorSystem("System" + id, ConfigFactory.load().getConfig("System" + id))
        val nodeActor = system.actorOf(Props(new Node(id, nod.nodes)), name = "node")
        nodeActor ! "START"

    }
}
