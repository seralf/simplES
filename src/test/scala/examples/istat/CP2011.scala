package examples.istat

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import simples.utilities.JSON
import csv.CSVParser
import java.nio.file.Paths
import simples.ES

object MainES extends App {

  val es = ES.local
  es.start()

  val (_index, _type) = ("taxonomy", "cp2011")

  // TEST data: GOT
  println("ES> indexing example data...")
  CP2011.data
    .zipWithIndex
    .foreach {
      case (doc, idx) =>
        val _id = doc.cod_5.replace(".", "-")
        val _source = JSON.writer.writeValueAsString(doc)
        es.indexer.index(_index, _type, _id)(_source)
    }

}

/**
 * esempio dati classificazione CP2011 di ISTAT
 *
 * TODO: ricostruire la gerarchia!
 *
 */
object CP2011 {

  def data = {

    val url = Paths.get("src/main/resources/data/ISTAT/cp2011_I-Vdigit.csv").toAbsolutePath().normalize().toUri().toURL().toString()
    CSVParser.fromURL(url)(delimiter = '"', separator = ';', encoding = "UTF-8")
      .parse[CP2011Item]()

  }

  def toJSONStream = {

    data
      .map(JSON.writer.writeValueAsString(_))

  }

}

case class CP2011Item(cod_5: String, nome_5: String, descr_5: String)

object MainCP2011Example extends App {

  val items = CP2011.data

  items
    .map { item =>

      val id = item.cod_5
      val path = id.split("\\.").toList

      (id, path, item.nome_5)

    }.foreach { item =>

      println(item)

    }

}
