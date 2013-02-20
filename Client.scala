package pl.chyla.chat

import scala.actors.{Actor, AbstractActor, TIMEOUT}
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node

object Client {
  
  val promptOut: String = "|>"
  val promptIn: String =  "<|"
  val timeout: Int = 6000
  
  var exit = false

  def main(args: Array[String]) : Unit = {

    if ((args.length < 2) || (args.length > 3)) {
      println("usage: scala pl.chyla.chat.Client [RemoteHostName] [RemotePort] [name]")
    
    } else {
      val (host, port) = (args(0), args(1).toInt)
      val name = if (args.length == 3) args(2) else "Default"

      val servNode = Node(host, port)
      val cli = new Client(servNode, name)
      cli.start

      while (!exit) {
        val msg = Console.readLine(Client.promptOut)
        if (msg == "exit") {
          exit = true
          cli  ! 'Disconnect
        } else {
          cli ! Send("[%s] %s".format(name, msg))
        }
      }
    }
  }
}

case class Send(message: String)
case class Connect(name: String)
case class Disconnect(name: String)



class Client(peer: Node, name: String) extends Actor {
  def act() {
    RemoteActor.classLoader = getClass().getClassLoader()
    val serv = select(peer, 'Serv)

    serv ! Connect(name)

    loop {
      react {
        case m:String => Console.printf("%s\n%s", m, Client.promptIn)
        case Send(msg) => serv ! msg
        case 'Disconnect =>  {
          serv ! Disconnect(name)
          exit
        }
        case 'Goaway => exitAs('Goaway)
        case _ => exit('Unknown)
      }
    }
  }

  def exitAs(reason: Any) {
    Client.exit = true
    printf("Server disconnected: %s" format reason)
    exit(reason)
  }
}
