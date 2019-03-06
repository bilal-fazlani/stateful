package stateful.events
import java.util.concurrent.{Executors, ScheduledExecutorService}

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

class Wiring {
  lazy val scheduledExecutorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
  lazy val singleEC: ExecutionContextExecutorService          = ExecutionContext.fromExecutorService(scheduledExecutorService)

  implicit lazy val system: ActorSystem = ActorSystem("stateful")
  implicit lazy val mat: Materializer   = ActorMaterializer()

  lazy val timer          = new Timer(scheduledExecutorService)
  lazy val ledger         = new Ledger(timer)
  lazy val accountFactory = new AccountFactory(ledger, mat)
}
