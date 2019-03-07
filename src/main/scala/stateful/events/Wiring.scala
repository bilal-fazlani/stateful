package stateful.events

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

class Wiring {

  def singleThreadedEc(): ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(Executors.newSingleThreadScheduledExecutor())

  implicit lazy val actorSystem: ActorSystem = ActorSystem("stateful")
  implicit lazy val mat: Materializer        = ActorMaterializer()

  lazy val timer          = new Timer(actorSystem)
  lazy val ledger         = new Ledger(timer)(singleThreadedEc())
  lazy val accountFactory = new AccountFactory(ledger, mat)
}
