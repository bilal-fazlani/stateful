package stateful.events

import ammonite.util.Res
import os.Path

import scala.concurrent.Future

class Setup(wiring: Wiring) {
  import wiring._
  import actorSystem.dispatcher

  val accounts: List[Account] = (1 to 10).toList.map(x => accountFactory.make(x))
  val wealthAccount           = new WealthAccount(accounts.take(2))(ecFactory.make(), mat)

  def runSetup(): Unit = {
    accounts.foreach { account =>
      Future.unit.foreach { _ =>
        (101 to 120).foreach { x =>
          Future.unit.foreach { _ =>
            account.deposit(x).failed.foreach(println)
          }
          Future.unit.foreach { _ =>
            account.withdraw(x).failed.foreach(println)
          }
        }
      }
    }
  }

  def shell(): (Res[Any], Seq[(Path, Long)]) =
    ammonite
      .Main(
        predefCode = """
                       |println("Starting Debugging!")
                       |import wiring._
                       |import setup._
                       |""".stripMargin
      )
      .run(
        "setup"  -> this,
        "wiring" -> wiring
      )

}
