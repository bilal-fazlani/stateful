package stateful.events

import akka.actor.ActorSystem

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}

class Timer(actorSystem: ActorSystem) {

  def tick(duration: FiniteDuration)(ec: ExecutionContext): Future[Unit] = {
    val p: Promise[Unit] = Promise()
    actorSystem.scheduler.scheduleOnce(duration: FiniteDuration)(p.success(()))(ec)
    p.future
  }

}
