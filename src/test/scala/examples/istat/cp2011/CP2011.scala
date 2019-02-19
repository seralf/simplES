package examples.istat.cp2011

import java.nio.file.Paths
import csv.CSVParser
import simples.utilities.JSON
import scala.reflect.api.materializeTypeTag

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
 * esempio dati classificazione CP2011 di ISTAT
 *
 * TODO: ricostruire la gerarchia!
 *
 */
object CP2011 {

  def toJavaList() = data.toList.asJava

  def data = {

    val url = Paths.get("src/main/resources/data/ISTAT/cp2011_I-Vdigit.csv")
      .toAbsolutePath().normalize().toUri().toURL().toString()

    CSVParser.fromURL(url)(delimiter = '"', separator = ';', encoding = "UTF-8")
      .parse[CP2011Item]()

  }

  def toJSONStream = {

    data
      .map(JSON.writer.writeValueAsString(_))

  }

}

case class CP2011Item(cod_5: String, nome_5: String, descr_5: String)

