package stateful.events

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

import scala.concurrent.{ExecutionContext, Future}

object OrElseTest extends App {

  trait Msg
  case class Add(x: Int) extends Msg
  case class GetTotal()  extends Msg

  def behaviourWithState(implicit ec: ExecutionContext): Behavior[Msg] = Behaviors.setup { ctx =>
    var total = 0

    Behaviors.receiveMessage {
      case Add(x) =>
        Future.unit.foreach { _ =>
          total += 1
        }
        Behaviors.same
      case GetTotal() =>
        println(total)
        Behaviors.same
    }
  }

  val behaviourWithSafeEc = BehaviourExtensions.withEc[Msg] { implicit ec =>
    behaviourWithState
  }

  val behaviourWithDefaultEc = Behaviors.setup[Msg] { ctx =>
    import ctx.executionContext
    behaviourWithState
  }

//  val test = ActorSystem(behaviourWithDefaultEc, "test")
  val test = ActorSystem(behaviourWithSafeEc, "test")

  import test.executionContext

  (1 to 10000).foreach { x =>
    Future.unit.foreach { _ =>
      test ! Add(x)
    }
  }

  Thread.sleep(2000)
  test ! GetTotal()

}
