package examples.got

import scala.collection.JavaConversions._
import simples.ES

/**
 * CHECK: http://localhost:9200/series/got/_search
 */
object MainES extends App {

  val es = ES.local
  es.start()

  val (_index, _type) = ("series", "got")

  // TEST data: GOT
  GOT_DATA.episodes_local
    .zipWithIndex
    .foreach {
      case (episode, idx) =>
        es.indexer.index(_index, _type, episode._1)(episode._2)
    }

}

