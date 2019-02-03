package examples.got

import scala.collection.JavaConversions._
import simples.ES
import simples.ESDocumentHandler

/**
 * CHECK: http://localhost:9200/series/got/_search
 */
object MainES extends App {

  val es = ES.local
  es.start()

  val (_index, _type) = ("series", "got")

  // TEST data: GOT
  println("ES> indexing example data...")
  GOT_DATA.episodes_local
    .zipWithIndex
    .foreach {
      case (episode, idx) =>
        es.indexer.index(_index, _type, episode._1)(episode._2)
    }

  //  CHECK Exception in thread "main" [series] IndexNotFoundException[no such index]

}

