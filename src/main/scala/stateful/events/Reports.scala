package stateful.events
import akka.NotUsed
import akka.stream.scaladsl.Source
import stateful.events.Action.{Deposit, Withdrawal}

object Reports {
  def aggregate(accounts: List[Account]): Source[Action, NotUsed] = {
    accounts.map(_.actionStream).foldLeft(Source.empty[Action])(_ merge _)
  }

  def aggregateBalance(accounts: List[Account]): Source[Int, NotUsed] = {
    aggregate(accounts).scan(0) {
      case (total, Deposit(_, amount))    => total + amount
      case (total, Withdrawal(_, amount)) => total - amount
    }
  }
}
