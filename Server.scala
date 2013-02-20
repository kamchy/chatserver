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

//stale clients problem
class ServerActor(port: Int) extends Actor {
  type Chatters = mutable.HashMap[String, OutputChannel[Any]]
  var chatters = new Chatters()

  def broadcast(msg: String, receivers: Iterator[OutputChannel[Any]] = chatters.valuesIterator) = receivers foreach (_ ! msg)

  def act() {
    RemoteActor.classLoader = getClass().getClassLoader()
    RemoteActor.alive(port)
    RemoteActor.register('Serv, this)

    loop {
      react {
        case Connect(name) =>  {
          chatters get(name) match {
            case Some(oldSender) => {
              sender ! "Name %s already exist".format(name)
              sender !'Goaway
            }
            case None => {
              chatters += (name->sender)
              broadcast(name + " connected")
            }
          }
        }
        case Disconnect(name) => {
          chatters -= name
          broadcast(name + " disconnected")
        }
        case msg: String =>  {
          Console.println(msg) 
          broadcast(msg, chatters.valuesIterator filter (_ != sender))
        }
      }
    }
  }
}
