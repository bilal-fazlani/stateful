package stateful.events

import akka.NotUsed
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.Source
import stateful.events.Action.{Deposit, Withdrawal}

import scala.concurrent.{ExecutionContext, Future}

class Account(accountNumber: Int, externalService: ExternalService, ledger: Ledger)(implicit singleThreadedEc: ExecutionContext,
                                                                                    mat: Materializer) {
  private var _balance = 0
  private var _actions = List.empty[Action]

  private val (queue, _stream) = Source.queue[Action](8192, OverflowStrategy.dropHead).preMaterialize()

  def deposit(amount: Int): Future[Unit] = Future.unit.flatMap { _ =>
    val deposit = Deposit(accountNumber, amount)
    externalService.update(deposit).map { x =>
      _balance += amount
      _actions ::= deposit
      ledger.record(deposit)
      queue.offer(deposit)
    }
  }

  def withdraw(amount: Int): Future[Unit] = Future.unit.flatMap { _ =>
    val withdrawal = Withdrawal(accountNumber, amount)
    externalService.update(withdrawal).map { _ =>
      _balance -= amount
      _actions ::= withdrawal
      ledger.record(withdrawal)
      queue.offer(withdrawal)
    }
  }

  def balance: Future[Int]                  = Future.unit.map(_ => _balance)
  def actions: Future[List[Action]]         = Future.unit.map(_ => _actions)
  def actionStream: Source[Action, NotUsed] = _stream
}

class AccountFactory(externalService: ExternalService, ledger: Ledger, mat: Materializer) {
  def make(accountNumber: Int, ec: ExecutionContext) = new Account(accountNumber, externalService, ledger)(ec, mat)
}
