package examples.istat

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import simples.utilities.JSON
import csv.CSVParser
import java.nio.file.Paths

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
      //      .parse[Map[String, _]]()
      .parse[CP2011Item]()

  }

  def toJSONStream = {

    data
      .map(JSON.writer.writeValueAsString(_))

  }

}

case class CP2011Item(cod_5: String, nome_5: String, descr_5: String)

object MainCP2011 extends App {

  CP2011.toJSONStream
    //  CP2011.toTree
    .zipWithIndex
    .foreach {
      case (json, i) =>
        println(i)
        println(json)
    }

}
