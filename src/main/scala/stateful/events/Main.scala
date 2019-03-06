package stateful.events

object Main {
  def main(args: Array[String]): Unit = {
    val wiring = new Wiring
    import wiring._

    val account1 = accountFactory.make(1, singleEC)
    val account2 = accountFactory.make(2, singleEC)

    Reports.aggregate(List(account1, account2)).runForeach(println)

    Thread.sleep(2000)
    account1.deposit(10)
    account2.deposit(100)
    account1.withdraw(5)
    account2.withdraw(50)
  }
}
