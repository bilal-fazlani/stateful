package stateful.events

sealed trait Action {
  def accountNumber: Int
  def amount: Int
}
object Action {
  case class Deposit(accountNumber: Int, amount: Int)    extends Action
  case class Withdrawal(accountNumber: Int, amount: Int) extends Action
}
