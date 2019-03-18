package stateful.events

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

import scala.concurrent.{ExecutionContext, Future}

object SafeEcTest extends App {

  trait Msg
  case class Add(x: Int)            extends Msg
  case class AddViaCallback(x: Int) extends Msg
  case class GetTotal()             extends Msg

  def behaviourWithState(implicit ec: ExecutionContext): Behavior[Msg] = Behaviors.setup { ctx =>
    var total = 0

    Behaviors.receiveMessage {
      case Add(x) =>
        total += 1
        Behaviors.same
      case AddViaCallback(x) =>
        Future.unit.foreach { _ =>
          total += 1
        }
        Behaviors.same
      case GetTotal() =>
        println(total)
        Behaviors.same
    }
  }

  val behaviourWithDefaultEc = Behaviors.setup[Msg] { ctx =>
    import ctx.executionContext
    behaviourWithState
  }

  val behaviourWithSafeEc = BehaviourExtensions.withSafeEc[Msg] { implicit ec =>
    behaviourWithState
  }

  val test = ActorSystem(behaviourWithDefaultEc, "test")
//  val test = ActorSystem(behaviourWithSafeEc, "test")
  println("*************")

  import test.executionContext

  (1 to 10000).foreach { x =>
    Future.unit.foreach { _ =>
      test ! Add(x)
    }
    Future.unit.foreach { _ =>
      test ! AddViaCallback(x)
    }
  }

  Thread.sleep(2000)
  test ! GetTotal()

}
