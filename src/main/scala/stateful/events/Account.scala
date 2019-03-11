package stateful.events

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.stream.{Materializer, OverflowStrategy}
import stateful.events.Action.{Deposit, Withdrawal}

import scala.concurrent.{ExecutionContext, Future}

class Account(accountNumber: Int, externalService: ExternalService, ledger: Ledger)(implicit ec: ExecutionContext,
                                                                                    mat: Materializer) {
  private var _balance = 0
  private var _actions = List.empty[Action]

  private val (queue, _stream) = Source.queue[Action](8192, OverflowStrategy.dropHead).preMaterialize()

  def deposit(amount: Int): Future[Unit] = Future.unit.flatMap { _ =>
    validate()
    val deposit = Deposit(accountNumber, amount)
    externalService.update(deposit).map { x =>
      _balance += amount
      _actions ::= deposit
      ledger.record(deposit)
      queue.offer(deposit)
    }
  }

  def withdraw(amount: Int): Future[Unit] = Future.unit.flatMap { _ =>
    validate()
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

  private def validate(): Unit = {
    val total = _actions.foldLeft(0) {
      case (acc, Deposit(_, amount))    => acc + amount
      case (acc, Withdrawal(_, amount)) => acc - amount
    }

    assert(total == _balance, throw new RuntimeException("can not reconcile the balance"))
  }
}

class AccountFactory(externalService: ExternalService, ledger: Ledger, mat: Materializer) {
  def make(accountNumber: Int, ec: ExecutionContext) = new Account(accountNumber, externalService, ledger)(ec, mat)
}
