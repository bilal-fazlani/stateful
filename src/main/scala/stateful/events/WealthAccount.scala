package stateful.events

import akka.stream.Materializer
import stateful.events.Action.{Deposit, Withdrawal}

import scala.concurrent.{ExecutionContext, Future}

class WealthAccount(accounts: List[Account])(implicit ec: ExecutionContext, mat: Materializer) {
  private var _balance = 0
  private var _actions = List.empty[Action]

  Streams
    .aggregate(accounts)
    .mapAsync(1) { x =>
      Future.unit.map { _ =>
        _actions ::= x
        x match {
          case Deposit(_, amount)    => _balance += amount
          case Withdrawal(_, amount) => _balance -= amount
        }
      }
    }
    .runForeach(_ => ())

  def balance: Future[Int]          = Future.unit.map(_ => _balance)
  def actions: Future[List[Action]] = Future.unit.map(_ => _actions)
}
