package stateful.events

import java.util.concurrent.ScheduledExecutorService

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}

class Timer(scheduledExecutorService: ScheduledExecutorService) {

  def delay[T](duration: FiniteDuration)(f: () => T): Future[T] = {
    val p: Promise[T] = Promise()
    scheduledExecutorService.schedule(() => p.success(f()), duration.length, duration.unit)
    p.future
  }

}
