package stateful.events

object Main {

  def main(args: Array[String]): Unit = {
    val wiring = new Wiring
    val setup  = new Setup(wiring)

    import wiring._
    import setup._

    setup.runSetup()
    Thread.sleep(2000)

    println("*********************")
    println(ledger.actionsFor(1).length)
    println(accounts(0).actions.length)
    println(ledger.actionsFor(1) == accounts(0).actions)
    println(accounts.forall(_.balance == 0))

//    setup.shell()
  }
}
