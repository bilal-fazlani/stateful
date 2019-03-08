package stateful.events

import org.scalatest.{FunSuite, Matchers}
import stateful.events.TestExtensions.FutureTestOnly

import scala.concurrent.Future

class BasicTest extends FunSuite with Matchers {

  val wiring = new Wiring
  val setup  = new Setup(wiring)

  import setup._
  import wiring._

  test("basic") {

    setup.runSetup()
    Thread.sleep(2000)

    ledger.actionsFor(1).get.length shouldBe 40
    accounts(0).actions.get.length shouldBe 40
    ledger.actionsFor(1).get shouldBe accounts(0).actions.get
    (accounts(0).actions.get ::: accounts(1).actions.get).toSet shouldBe wealthAccount.actions.get.toSet
    accounts.forall(_.balance.get == 0) shouldBe true
    wealthAccount.balance.get shouldBe 0
  }

  test("simple-reader") {
    val persons = (1 to 26)
      .zip('a' to 'z')
      .map {
        case (n, s) => Person(n, s.toString)
      }
      .toSet

    import actorSystem.dispatcher

    val eventualPersons = Future.unit.flatMap { _ =>
      val futures = (1 to 100000).map { _ =>
        Future.unit.flatMap(_ => simpleReader.person)
      }
      Future.sequence(futures)
    }

    Future.unit.foreach { _ =>
      (1 to 10000).flatMap(_ => persons).foreach { p =>
        Future.unit.foreach { _ =>
          simpleReader.update(p)
        }
      }
    }

    persons shouldBe eventualPersons.get.toSet
  }

}
