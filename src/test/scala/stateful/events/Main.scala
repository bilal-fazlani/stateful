package stateful.events

object Main {

  def main(args: Array[String]): Unit = {
    val wiring = new Wiring
    val setup  = new Setup(wiring)

    import setup._
    import wiring._

    setup.runSetup()
    Streams.aggregate(accounts).runForeach(println)

    Thread.sleep(2000)

    println("*********************")

    setup.shell()
  }
}
