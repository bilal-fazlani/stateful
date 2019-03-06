package stateful.events

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationDouble

class Ledger(timer: Timer)(implicit ec: ExecutionContext) {
  //public only for demo/debugging
  var allActions = List.empty[Action]

  def record(action: Action): Future[Unit] = timer.delay(1.seconds)(() => allActions ::= action)

  def actionsFor(accountNumber: Int): List[Action] = allActions.filter(_.accountNumber == accountNumber)
}
