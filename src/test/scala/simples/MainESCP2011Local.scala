package simples

import examples.istat.cp2011.CP2011
import simples.utilities.JSON

object MainESCP2011Local extends App {

  val (_index, _doc) = ("cp2011", "doc")

  val es = ESHelper.local("src/main/resources/conf/es-local.conf")
  es.start()

  es.index_delete(_index)
  es.index_create(_index, _doc)("src/main/resources/data/ISTAT/_settings.json", "src/main/resources/data/ISTAT/_mapping.json")

  // indexing example data
  CP2011.data("src/main/resources/data/ISTAT/cp2011_I-Vdigit.csv")
    .zipWithIndex
    .foreach {
      case (doc, i) =>

        val _id = String.format("%04d", Integer.valueOf(i.toString))
        val _source = JSON.writer.writeValueAsString(doc)

        es.indexing(_index, _doc, _id, _source)
    }

  val test = es.index_exists(_index)
  println(s"index ${_index} created? ${test}")

  Thread.sleep(120000)
  es.stop()

}