package stateful.events

import akka.NotUsed
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.Source
import stateful.events.Action.{Deposit, Withdrawal}

import scala.concurrent.{ExecutionContext, Future}

class Account(accountNumber: Int, ledger: Ledger)(implicit ec: ExecutionContext, mat: Materializer) {
  //public only for demo/debugging
  var balance = 0
  //public only for demo/debugging
  var actions = List.empty[Action]

  private val (queue, _stream) = Source.queue[Action](8192, OverflowStrategy.dropHead).preMaterialize()

  def deposit(amount: Int): Future[Unit] = Future.unit.flatMap { _ =>
    val deposit = Deposit(accountNumber, amount)
    ledger.record(deposit).map { x =>
      balance += amount
      actions ::= deposit
      queue.offer(deposit)
    }
  }

  def withdraw(amount: Int): Future[Unit] = Future.unit.flatMap { _ =>
    val withdrawal = Withdrawal(accountNumber, amount)
    ledger.record(withdrawal).map { _ =>
      balance -= amount
      actions ::= withdrawal
      queue.offer(withdrawal)
    }
  }

  def actionStream: Source[Action, NotUsed] = _stream
}

class AccountFactory(ledger: Ledger, mat: Materializer) {
  def make(accountNumber: Int, ec: ExecutionContext) = new Account(accountNumber, ledger)(ec, mat)
}
