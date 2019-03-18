package stateful.events

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

object BehaviourExtensions {

  def withSafeEc[T: ClassTag](factory: ExecutionContext => Behavior[T]): Behavior[T] = {
    withRunnableRef[T] { actorRef =>
      val ec = new ExecutionContext {
        override def execute(runnable: Runnable): Unit     = actorRef ! runnable
        override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
      }
      factory(ec)
    }
  }

  def withRunnableRef[T: ClassTag](factory: ActorRef[Runnable] => Behavior[T]): Behavior[T] = {
    val widenBehaviour = Behaviors.setup[Any] { ctx =>
      factory(ctx.self).widen[Any] {
        case x: T => x
      }
    }
    val runnable = Behaviors.receiveMessagePartial[Any] {
      case x: Runnable =>
        x.run()
        Behaviors.same
    }
    widenBehaviour.orElse(runnable).narrow[T]
  }

}
