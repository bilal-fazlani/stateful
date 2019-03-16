package stateful.events

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

object OrElseTest extends App {

  case class A(x: Int)

  val runnableBehaviour: Behavior[Runnable] = Behaviors.receiveMessage { runnable =>
    println()
    println("pre-run")
    runnable.run()
    println("post-run")
    Behaviors.same
  }

  def withRef[T](factory: ActorRef[Runnable] => Behavior[T]): Behavior[T] = {
    val widenBehaviour = Behaviors.setup[Any] { ctx =>
      factory(ctx.self)
        .widen[Any] {
          case x: T => x
        }
    }
    val widenRunnable = runnableBehaviour.widen[Any] {
      case x: Runnable => x
    }
    widenRunnable.orElse(widenBehaviour).narrow[T]
  }

  lazy val behaviour = withRef[A] { actorRef =>
    Behaviors.receiveMessage[A] {
      case A(x) =>
        println("a")
        actorRef ! (() => println(s"executing $x"))
        Behaviors.same
    }
  }

  val test = ActorSystem(behaviour, "test")

  test ! A(1)
  test ! A(2)
  test ! A(3)
  test ! A(4)
  test ! A(5)

}
