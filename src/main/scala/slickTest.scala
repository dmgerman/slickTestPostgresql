//
// Based on tutorial by Stephan February
// found at http://www.hashbangbin.sh/posts/getting-started-scala-%2B-slick-%2B-postgresql-mysql/
//

import slick.driver.PostgresDriver.api._
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

// Definition of the suppliers table


class Suppliers(tag: Tag) extends Table[(Int, String, String, String, String, String)](tag, "suppliers") {
  def id = column[Int]("sup_id", O.PrimaryKey) // This is the primary key column
  def name = column[String]("sup_name")
  def street = column[String]("street")
  def city = column[String]("city")
  def state = column[String]("state")
  def zip = column[String]("zip")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, name, street, city, state, zip)
}

object Application extends  App {

  val suppliers = TableQuery[Suppliers]

  // make sure application.conf points to the right database and
  // has correct user/password info

  val db = Database.forConfig("postgresDB")

  // direct statement sent to the database. useful when you don't know how to do something with slick
  // or it is too cumbersome

  def qDropSchema = sqlu"""drop table if exists suppliers;""";

  try {
    Await.result(db.run(DBIO.seq(
      qDropSchema
    )), Duration.Inf)
  }
  catch {
    case _: Throwable => println(Console.RED +  "Go an exception. Probably the database parms are not properly set upt " + Console.RESET )
  }

  // using the functional interface

  // create the table first

  val setup = DBIO.seq( suppliers.schema.create)

  Await.result(db.run(setup), Duration.Inf)


  val insert = DBIO.seq(
    suppliers += (101, "Acme, Inc.",      "99 Market Street", "Groundsville", "CA", "95199"),
    suppliers += ( 49, "Superior Coffee", "1 Party Place",    "Mendocino",    "CA", "95460")
  )

  Await.result(db.run(insert), Duration.Inf)

  val insert2 = DBIO.seq(
    suppliers += ( 3, "Habit Coffee", "552 Pandora St",    "Victoria",    "BC", "V8S1W7")
  )

  Await.result(db.run(insert2), Duration.Inf)

  // query all tuples in the table
  Await.result(
      db.run(suppliers.result).map(_.foreach {
        case (id, name, street, city, state, zip) => println(s"${name}: ${street} : ${city}")
      }), Duration.Inf)

  println("----------------------------------------")

  val q = suppliers.filter(_.city === "Victoria")

  Await.result(
    db.run(q.result).map(_.foreach {
      case (id, name, street, city, state, zip) => println(s"${name}: ${street} : ${city}")
    }),
    Duration.Inf)
  

  db.close

}
