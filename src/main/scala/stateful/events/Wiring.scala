package stateful.events

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import stateful.events.framework.ExecutionContextFactory

class Wiring {

  implicit lazy val actorSystem: ActorSystem = ActorSystem("stateful")
  implicit lazy val mat: Materializer        = ActorMaterializer()

  lazy val timer = new Timer(actorSystem)

  val ecFactory: ExecutionContextFactory = ExecutionContextFactory.streamBased
//  val ecFactory: ExecutionContextFactory = ExecutionContextFactory.singleThreaded
  lazy val ledger          = new Ledger()(ecFactory.make())
  lazy val externalService = new ExternalService(timer)
  lazy val accountFactory  = new AccountFactory(externalService, ledger, mat)
}
