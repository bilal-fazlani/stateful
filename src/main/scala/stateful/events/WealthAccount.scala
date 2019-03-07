package stateful.events

import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

class WealthAccount(accounts: List[Account])(implicit ec: ExecutionContext, mat: Materializer) {
  private var _balance = 0
  private var _actions = List.empty[Action]

  Streams
    .aggregate(accounts)
    .mapAsync(1) { x =>
      Future.unit.map(_ => _actions ::= x)
    }
    .runForeach(_ => ())

  Streams
    .aggregateBalance(accounts)
    .mapAsync(1) { x =>
      Future.unit.map(_ => _balance = x)
    }
    .runForeach(_ => ())

  def balance: Future[Int]          = Future.unit.map(_ => _balance)
  def actions: Future[List[Action]] = Future.unit.map(_ => _actions)
}
