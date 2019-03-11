package stateful.events.framework

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter.UntypedActorSystemOps
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.Source
import stateful.events.framework.ExecutorActor.{Execute, Msg}

import scala.concurrent.ExecutionContext

trait ExecutionContextFactory {
  def make(): ExecutionContext
}

object ExecutionContextFactory {
  val singleThreaded: ExecutionContextFactory = () => ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())

  def streamBased(implicit mat: Materializer): ExecutionContextFactory = { () =>
    new ExecutionContext {
      private val (queue, stream) = Source.queue[Runnable](1024, OverflowStrategy.dropHead).preMaterialize()
      stream.runForeach(_.run())

      override def execute(runnable: Runnable): Unit     = queue.offer(runnable)
      override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
    }
  }

}
