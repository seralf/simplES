package simples

import simples.utilities.JSON
import examples.istat.cp2011.CP2011
import org.elasticsearch.action.bulk.BulkRequestBuilder

object MainESCP2011 extends App {

  val (_index, _doc) = ("cp2011", "doc")

  val es = ESHelper.transport("src/main/resources/conf/es.conf")
  es.start()

  es.index_delete(_index)
  es.index_create(_index, _doc)("src/main/resources/data/ISTAT/_settings.json", "src/main/resources/data/ISTAT/_mapping.json")

  // indexing example data
  CP2011.data
    .zipWithIndex
    .foreach {
      case (doc, i) =>

        val _id = String.format("%04d", Integer.valueOf(i.toString))
        val _source = JSON.writer.writeValueAsString(doc)

        es.indexing(_index, _doc, _id, _source)
    }

  es.stop()

}