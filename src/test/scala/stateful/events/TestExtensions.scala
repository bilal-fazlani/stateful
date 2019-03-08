package stateful.events

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

object TestExtensions {
  implicit class FutureTestOnly[T](f: Future[T]) {
    def get: T = Await.result(f, 10.seconds)
  }
}
