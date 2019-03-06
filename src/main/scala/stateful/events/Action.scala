package stateful.events

sealed trait Action
object Action {
  case class Deposit(accountNumber: Int, amount: Int)    extends Action
  case class Withdrawal(accountNumber: Int, amount: Int) extends Action
}
