package stateful.events

import scala.concurrent.{ExecutionContext, Future}

class Ledger(implicit singleThreadedEc: ExecutionContext) {
  private var _allActions = List.empty[Action]

  def record(action: Action): Future[Unit] = Future.unit.map { _ =>
    _allActions ::= action
  }

  def actionsFor(accountNumber: Int): Future[List[Action]] = Future.unit.map { _ =>
    _allActions.filter(_.accountNumber == accountNumber)
  }

  def allActions: Future[List[Action]] = Future.unit.map(_ => _allActions)
}
