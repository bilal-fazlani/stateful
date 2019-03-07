package stateful.events

import akka.actor.ActorSystem

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}

class Timer(actorSystem: ActorSystem) {

  import actorSystem.dispatcher

  def tick(duration: FiniteDuration): Future[Unit] = {
    val p: Promise[Unit] = Promise()
    actorSystem.scheduler.scheduleOnce(duration: FiniteDuration)(p.success(()))
    p.future
  }

}
