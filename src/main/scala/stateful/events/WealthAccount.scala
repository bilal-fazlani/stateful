package stateful.events

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import stateful.events.Action.{Deposit, Withdrawal}

import scala.concurrent.{ExecutionContext, Future}

class WealthAccount(accounts: List[Account])(implicit ec: ExecutionContext, mat: Materializer) {
  private var _balance = 0
  private var _actions = List.empty[Action]

  accounts
    .map(_.actionStream)
    .foldLeft(Source.empty[Action])(_ merge _)
    .mapAsync(1) { action =>
      Future.unit.map { _ =>
        _actions ::= action
        action match {
          case Deposit(_, amount)    => _balance += amount
          case Withdrawal(_, amount) => _balance -= amount
        }
        action
      }
    }
    .runForeach { action =>
      println((action, _balance))
    }

  def balance: Future[Int]          = Future.unit.map(_ => _balance)
  def actions: Future[List[Action]] = Future.unit.map(_ => _actions)
  def actionsFor(accountNumber: Int): Future[List[Action]] = Future.unit.map { _ =>
    _actions.filter(_.accountNumber == accountNumber)
  }
}
