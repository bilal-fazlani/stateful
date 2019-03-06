package stateful.events

import scala.concurrent.Future
import scala.concurrent.duration.DurationDouble

class Ledger(timer: Timer) {
  private var allActions = List.empty[Action]

  def record(action: Action): Future[Unit] = timer.delay(1.seconds)(() => allActions ::= action)
}
