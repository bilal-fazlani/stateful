package stateful.events

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

object OrElseTest extends App {
  case class A()
  case class Exec(f: Runnable)

  def a0Beh(actorRef: ActorRef[Exec]): Behavior[A] = Behaviors.receiveMessage[A] {
    case A() =>
      println("a")
      actorRef ! Exec(() => println("executing"))
      Behaviors.same
  }

  lazy val execBeh = Behaviors.receiveMessagePartial[Any] {
    case Exec(runnable) =>
      println("*************")
      runnable.run()
      println("b")
      Behaviors.same
  }

  lazy val beh: Behavior[A] = Behaviors
    .setup[Any] { ctx =>
      a0Beh(ctx.self)
        .widen[Any] {
          case x: A => x
        }
        .orElse(execBeh)
    }
    .narrow[A]

  def withEc[T](f: ActorRef[Exec] => Behavior[T]): Behavior[T] = {
    val value = Behaviors.setup[Any] { ctx =>
      f(ctx.self)
        .widen[Any] {
          case x: T =>
            println("widening")
            x
        }
    }
    execBeh.orElse { value }.narrow[T]
  }

  lazy val beh2 = withEc { actorRef =>
    Behaviors.receiveMessage[A] {
      case A() =>
        println("a")
        actorRef ! Exec(() => println("executing"))
        Behaviors.same
    }
  }

  val test = ActorSystem(beh2, "test")

  test ! A()
  Thread.sleep(1000)
  test ! A()

}
