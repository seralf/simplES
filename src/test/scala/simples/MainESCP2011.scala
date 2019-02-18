package simples

import simples.utilities.JSON
import examples.istat.cp2011.CP2011
import org.elasticsearch.action.bulk.BulkRequestBuilder
import java.nio.file.Paths
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.elasticsearch.common.settings.Settings
import scala.util.Success
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
  CP2011.data
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

//object MainESCP2011Local extends App {
//
//  val (_index, _doc) = ("cp2011", "doc")
//
//  // ----------------------------------------------------------------
//  val _file = Paths.get("src/main/resources/conf/es-local.conf").toAbsolutePath().normalize().toFile()
//  val settings_content = ConfigFactory.empty()
//    .withFallback(ConfigFactory.parseFileAnySyntax(_file))
//  val settings = settings_content.getConfig("elasticsearch").entrySet()
//    .foldRight(Settings.builder())((e, builder) => builder.put(e.getKey, e.getValue.unwrapped().toString()))
//    .build()
//
//  val node = EmbeddedNode(settings)
//
//  // ----------------------------------------------------------------
//
//  //  val es = ESHelper.local("src/main/resources/conf/es-local.conf")
//  val es = ESHelper.transport("src/main/resources/conf/es.conf")
//  es.start()
//
//  es.index_delete(_index)
//  es.index_create(_index, _doc)("src/main/resources/data/ISTAT/_settings.json", "src/main/resources/data/ISTAT/_mapping.json")
//
//  // indexing example data
//  CP2011.data
//    .zipWithIndex
//    .foreach {
//      case (doc, i) =>
//
//        val _id = String.format("%04d", Integer.valueOf(i.toString))
//        val _source = JSON.writer.writeValueAsString(doc)
//
//        es.indexing(_index, _doc, _id, _source)
//    }
//
//  val test = es.index_exists(_index)
//  println(s"index ${_index} created? ${test}")
//
//  Thread.sleep(120000)
//  es.stop()
//
//}