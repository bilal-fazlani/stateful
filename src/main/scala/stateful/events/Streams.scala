package stateful.events
import akka.NotUsed
import akka.stream.scaladsl.Source
import stateful.events.Action.{Deposit, Withdrawal}

object Streams {
  def aggregate(accounts: List[Account]): Source[Action, NotUsed] = {
    accounts.map(_.actionStream).foldLeft(Source.empty[Action])(_ merge _)
  }

  def aggregateBalance(accounts: List[Account]): Source[Int, NotUsed] = {
    aggregate(accounts).scan(0) {
      case (acc, Deposit(_, amount))    => acc + amount
      case (acc, Withdrawal(_, amount)) => acc - amount
    }
  }
}
