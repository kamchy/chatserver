package pl.chyla.chat

import scala.actors.Actor
import scala.actors.Actor._
import scala.actors.remote.RemoteActor
import scala.actors.OutputChannel

import scala.collection.mutable

object Server {
  def main(args: Array[String]) : Unit = {
    if (args.length == 1) {
      val port = args(0).toInt
      val serv = new ServerActor(port)
      serv.start()
    }
    else {
      println("usage: scala pl.chyla.chat.Server [LocalPort]")
    }
  }
}

class ServerActor(port: Int) extends Actor {
  var chatters = mutable.HashSet[OutputChannel[Any]]()

  def act() {
    RemoteActor.alive(port)
    RemoteActor.register('Serv, this)

    loop {
      react {
        case 'Connect =>  chatters += sender
        case 'Disconnect => chatters -= sender
        case msg =>  {
          Console.println(msg) 
          chatters filter (c => c != sender) foreach(_ ! msg)
        }
      }
    }
  }
}
