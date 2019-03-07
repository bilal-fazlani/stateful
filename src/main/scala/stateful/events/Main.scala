package stateful.events
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

object Main {

  def main(args: Array[String]): Unit = {
    val wiring = new Wiring
    val setup  = new Setup(wiring)

    import wiring._
    import setup._

    setup.runSetup()
    Thread.sleep(2000)

    println("*********************")
    println(ledger.actionsFor(1).get.length)
    println(accounts(0).actions.get.length)
    println(ledger.actionsFor(1).get == accounts(0).actions.get)
    println((accounts(0).actions.get ::: accounts(1).actions.get).toSet == wealthAccount.actions.get.toSet)
    println(accounts.forall(_.balance.get == 0))
    println(wealthAccount.balance.get == 0)

//    setup.shell()
  }

  implicit class FutureTestOnly[T](f: Future[T]) {
    def get: T = Await.result(f, 10.seconds)
  }
}
