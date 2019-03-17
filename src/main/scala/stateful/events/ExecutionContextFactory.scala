package stateful.events

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.UntypedActorSystemOps
import akka.stream.scaladsl.Source
import akka.stream.{Materializer, OverflowStrategy}

import scala.concurrent.ExecutionContext

trait ExecutionContextFactory {
  def make(): ExecutionContext
}

object ExecutionContextFactory {

  def default(implicit actorSystem: ActorSystem): ExecutionContextFactory = () => actorSystem.dispatcher

  val singleThreaded: ExecutionContextFactory = { () =>
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())
  }

  val multiThreaded: ExecutionContextFactory = { () =>
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(20))
  }

  def streamBased(implicit mat: Materializer): ExecutionContextFactory = { () =>
    val (queue, stream) = Source.queue[Runnable](1024, OverflowStrategy.dropHead).preMaterialize()
    stream.runForeach(_.run())

    new ExecutionContext {
      override def execute(runnable: Runnable): Unit     = queue.offer(runnable)
      override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
    }
  }

  def actorBased(implicit actorSystem: ActorSystem): ExecutionContextFactory = { () =>
    val actorRef = actorSystem.spawnAnonymous(runnableBehaviour)
    new ExecutionContext {
      override def execute(runnable: Runnable): Unit     = actorRef ! runnable
      override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
    }
  }

  val synchronous: ExecutionContextFactory = { () =>
    new ExecutionContext {
      override def execute(runnable: Runnable): Unit     = runnable.run()
      override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
    }
  }

  private val runnableBehaviour: Behavior[Runnable] = Behaviors.receiveMessage { x =>
    x.run()
    Behaviors.same
  }
}
