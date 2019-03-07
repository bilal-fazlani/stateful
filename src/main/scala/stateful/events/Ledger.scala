package stateful.events

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationDouble

class Ledger(timer: Timer)(implicit singleThreadedEc: ExecutionContext) {
  //public only for demo/debugging
  var allActions = List.empty[Action]

  def record(action: Action)(ec: ExecutionContext): Future[Unit] = {
    val tick = timer.tick(1.seconds)(ec)
    tick.map(_ => allActions ::= action)
  }

  def actionsFor(accountNumber: Int): List[Action] = allActions.filter(_.accountNumber == accountNumber)
}
