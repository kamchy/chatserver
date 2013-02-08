package pl.chyla.chat

import scala.actors.Actor
import scala.actors.remote.RemoteActor
import scala.actors.remote.RemoteActor._
import scala.actors.remote.Node

object Client {
  
  val prompt: String = "|>"
  def main(args: Array[String]) : Unit = {

    if ((args.length < 2) || (args.length > 3)) {
      println("usage: scala pl.chyla.chat.Client [RemoteHostName] [RemotePort] [name]")
    
    } else {
      val (host, port) = (args(0), args(1).toInt)
      val name = if (args.length == 3) args(2) else "Default"

      val servNode = Node(host, port)
      val cli = new Client(servNode, name)
      cli.start


      var exit = false
      while (!exit) {
        val msg = Console.readLine()
        if (msg == "exit") {
          exit = true
          cli ! Disconnect
        } else {
          cli ! Send("[%s] %s".format(name, msg))
          Console.print(prompt)
        }
        
      }
    }
  }
}

case class Send(message: String)
case object Disconnect

class Client(peer: Node, name: String) extends Actor {
  def act() {
    val serv = select(peer, 'Serv)
    serv ! 'Connect
    loop {
      react {
        case m:String => Console.printf("%s\n%s", m, Client.prompt)
        case Send(msg) => serv ! msg
        case Disconnect =>  {
          serv ! 'Disconnect
          exit
        }
        case _ =>
      }
    }
  }
}
