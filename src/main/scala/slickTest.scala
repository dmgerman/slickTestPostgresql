import slick.driver.PostgresDriver.api._
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

// Definition of the SUPPLIERS table
//This is a verbatim copy of an example Table config which you can find HERE:
//http://slick.typesafe.com/doc/3.1.0/gettingstarted.html
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

  //Choose your flavour. One only. The config string refers
  //to settings in application.conf
  val db = Database.forConfig("postgresDB")

  // direct statement sent to the database

  def qDropSchema = sqlu"""drop table if exists suppliers;""";

  try{
    Await.result(db.run(DBIO.seq(
      qDropSchema
    )), Duration.Inf)
  }

  // using the functional interface

  // create the table first

  val setup = DBIO.seq( suppliers.schema.create)

  try{
    Await.result(db.run(setup), Duration.Inf)
  }


  val insert = DBIO.seq(
    suppliers += (101, "Acme, Inc.",      "99 Market Street", "Groundsville", "CA", "95199"),
    suppliers += ( 49, "Superior Coffee", "1 Party Place",    "Mendocino",    "CA", "95460")
  )

  try{
    Await.result(db.run(insert), Duration.Inf)
  }

  val insert2 = DBIO.seq(
    suppliers += ( 3, "Habit Coffee", "190 Pandora St",    "Victoria",    "BC", "V8S1W7")
  )

  try{
    Await.result(db.run(insert2), Duration.Inf)
  }


  try {
    Await.result(
      db.run(suppliers.result).map(_.foreach {
          case (id, name, street, city, state, zip) => println(s"${name}: ${street} : ${city}")
      }),
      Duration.Inf)
  }

  println("----------------------------------------")

  try {
    val q = suppliers.filter(_.city === "Victoria")
    Await.result(
      db.run(q.result).map(_.foreach {
          case (id, name, street, city, state, zip) => println(s"${name}: ${street} : ${city}")
      }),
      Duration.Inf)
  }

  

  db.close

}
