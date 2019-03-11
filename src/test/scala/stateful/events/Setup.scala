package stateful.events

import ammonite.util.Res
import os.Path

import scala.concurrent.Future

class Setup(wiring: Wiring) {
  import wiring._
  import actorSystem.dispatcher

  val accounts: List[Account] = (1 to 10).toList.map(x => accountFactory.make(x, ecFactory.make()))
  val wealthAccount           = new WealthAccount(accounts.take(2))(ecFactory.make(), mat)

  def runSetup(): Unit = {
    accounts.foreach { account =>
      val futures = (101 to 120).map(x => Future(x))
      futures.foreach { future =>
        future.foreach { x =>
          account.deposit(x)
        }
        future.foreach { x =>
          account.withdraw(x)
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
        "setup"   -> this,
        "reports" -> Streams,
        "wiring"  -> wiring
      )

}
