package stateful.events

import akka.actor.ActorSystem

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}

class Timer(actorSystem: ActorSystem) {

  def delay[T](duration: FiniteDuration)(f: () => T)(implicit ec: ExecutionContext): Future[T] = {
    val p: Promise[T] = Promise()
    actorSystem.scheduler.scheduleOnce(duration: FiniteDuration)(p.success(f()))
    p.future
  }

}
