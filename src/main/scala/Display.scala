package upmc.akka.culto

import akka.actor.{Actor}

class Display extends Actor {

  def receive = {

    case print(message:String) => {

      println(message)

    }

  }

}
