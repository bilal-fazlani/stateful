package stateful.events

import java.util.concurrent.Executors

import akka.stream.scaladsl.Source
import akka.stream.{Materializer, OverflowStrategy}

import scala.concurrent.ExecutionContext

trait ExecutionContextFactory {
  def make(): ExecutionContext
}

object ExecutionContextFactory {
  val singleThreaded: ExecutionContextFactory = { () =>
    ExecutionContext.fromExecutorService(Executors.newSingleThreadExecutor())
  }

  val multiThreaded: ExecutionContextFactory = { () =>
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(20))
  }

  val synchronous: ExecutionContextFactory = { () =>
    new ExecutionContext {
      override def execute(runnable: Runnable): Unit     = runnable.run()
      override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
    }
  }

  def streamBased(implicit mat: Materializer): ExecutionContextFactory = { () =>
    new ExecutionContext {
      private val (queue, stream) = Source.queue[Runnable](1024, OverflowStrategy.dropHead).preMaterialize()
      stream.runForeach(_.run())

      override def execute(runnable: Runnable): Unit     = queue.offer(runnable)
      override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
    }
  }

}
