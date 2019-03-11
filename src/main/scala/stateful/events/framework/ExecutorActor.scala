package stateful.events.framework

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object ExecutorActor {
  trait Msg
  case class Execute(f: () => Unit) extends Msg
  case object Stop                  extends Msg

  val behaviour: Behavior[Msg] = Behaviors.receiveMessage {
    case Execute(f) => f(); Behaviors.same
    case Stop       => Behaviors.stopped
  }
}
