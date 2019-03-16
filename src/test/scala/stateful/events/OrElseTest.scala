package stateful.events

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

object OrElseTest extends App {

  trait Msg
  case class Add(x: Int) extends Msg
  case class GetTotal()  extends Msg

  def withRef[T: ClassTag](factory: ActorRef[Runnable] => Behavior[T]): Behavior[T] = {
    val widenBehaviour = Behaviors.setup[Any] { ctx =>
      factory(ctx.self)
        .widen[Any] {
          case x: T => x
        }
    }
    val widenRunnable = Behaviors.receiveMessagePartial[Any] {
      case runnable: Runnable =>
        runnable.run()
        Behaviors.same
    }
    widenBehaviour.orElse(widenRunnable).narrow[T]
  }

  def withEc[T: ClassTag](factory: ExecutionContext => Behavior[T]): Behavior[T] = {
    withRef[T] { actorRef =>
      val ec = new ExecutionContext {
        override def execute(runnable: Runnable): Unit     = actorRef ! runnable
        override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
      }
      factory(ec)
    }
  }

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

  val behaviourWithSafeEc = withEc[Msg] { implicit ec =>
    behaviourWithState
  }

  val behaviourWithDefaultEc = Behaviors.setup[Msg] { ctx =>
    import ctx.executionContext
    behaviourWithState
  }

  val test = ActorSystem(behaviourWithDefaultEc, "test")
//  val test = ActorSystem(behaviourWithSafeEc, "test")

  import test.executionContext

  (1 to 10000).foreach { x =>
    Future.unit.foreach { _ =>
      test ! Add(x)
    }
  }

  Thread.sleep(2000)
  test ! GetTotal()

}
