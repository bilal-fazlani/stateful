package stateful.events.framework

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter.UntypedActorSystemOps
import stateful.events.framework.ExecutorActor.{Execute, Msg}

import scala.concurrent.ExecutionContext

trait ExecutionContextFactory {
  def make(): ExecutionContext
}

object ExecutionContextFactory {
  val singleThreaded: ExecutionContextFactory = () => ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  def actorBased(actorSystem: ActorSystem): ExecutionContextFactory = { () =>
    new ExecutionContext {
      private val actorRef: ActorRef[Msg] = actorSystem.spawnAnonymous(ExecutorActor.behaviour)

      override def execute(runnable: Runnable): Unit     = actorRef ! Execute(() => runnable.run())
      override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
    }
  }
}
