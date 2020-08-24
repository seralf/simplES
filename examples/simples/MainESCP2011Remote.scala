package simples

import examples.istat.cp2011.CP2011
import scala.util.Success
import simples.utilities.JSON
import scala.util.Failure

object MainESCP2011Remote extends App {

  val (_index, _doc) = ("cp2011", "doc")

  val es = ESHelper.remote("src/main/resources/conf/es.conf")
  es.start()

  println("\n\n")
  es.index_delete(_index) match {

    case Success(ok) => println(s"ES> index ${_index} DELETED")
    case Failure(ko) => System.err.println(s"ES> index ${_index} DELETE ERROR")

  }

  println("\n\n")
  es.index_create(_index, _doc)("src/main/resources/data/ISTAT/_settings.json", "src/main/resources/data/ISTAT/_mapping.json") match {

    case Success(ok) => println(s"ES> index ${_index} CREATED")
    case Failure(ko) =>
      System.err.println(s"ES> index ${_index} CREATION ERROR")
      ko.printStackTrace()
  }

  // indexing example data
  CP2011.data("src/main/resources/data/ISTAT/cp2011_I-Vdigit.csv")
    .zipWithIndex
    .foreach {
      case (doc, i) =>

        val _id = String.format("%04d", Integer.valueOf(i.toString))
        val _source = JSON.writer.writeValueAsString(doc)

        es.indexing(_index, _doc, _id, _source)
    }

  //  Thread.sleep(120000)
  es.stop()

}