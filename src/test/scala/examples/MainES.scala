package examples

import simples.ES
import simples.utilities.JSON
import scala.util.Success
import scala.util.Failure
import examples.istat.CP2011

object MainSimplES extends App {

  val (_index, _type) = ("taxonomy", "cp2011")

  val es = ES.remote

  val client = es.client

  es.start().get

  println("\nES> indexing example data...")
  val data = CP2011.data

  println(s"\n\npreparing ${data.size} docs\n\n")

  data.zipWithIndex
    .foreach {
      case (doc, idx) =>

        val _id = doc.cod_5.replace(".", "-")
        val _source = JSON.writer.writeValueAsString(doc)
        es.indexer.index(_index, _type, _id)(_source).get

        println(s"ES> added doc n°\t${idx}")

    }

}

