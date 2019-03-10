package stateful.events

import scala.concurrent.{ExecutionContext, Future}

case class Person(age: Int, name: String)

class SimpleReader(implicit ec: ExecutionContext) {
  private var _person = Person(1, "a")

  def update(person: Person): Future[Unit] = Future.unit.map { _ =>
    _person = person
  }

  def person: Future[Person] = Future.unit.map { _ =>
    _person
  }
}
