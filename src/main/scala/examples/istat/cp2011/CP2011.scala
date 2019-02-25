package examples.istat.cp2011

import java.nio.file.Paths
import csv.CSVParser
import simples.utilities.JSON
import scala.reflect.api.materializeTypeTag

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import simples.utilities.ModelAdapter

object MainCP2011 extends App {

  CP2011.data("src/main/resources/data/ISTAT/cp2011_I-Vdigit.csv")
    .foreach { item =>
      println(item)
    }

}

/**
 * esempio dati classificazione CP2011 di ISTAT
 *
 * TODO: ricostruire la gerarchia!
 *
 */
object CP2011 {

  def toJavaList(filename: String) = data(filename).toList.asJava

  def data(filename: String) = {

    val url = Paths.get(filename)
      .toAbsolutePath().normalize().toUri().toURL().toString()

    CSVParser.fromURL(url)(delimiter = '"', separator = ';', encoding = "UTF-8")
      .parse[Map[String, String]]()
      .map { map =>
        // IDEA: generalize wildcards, here!
        Map(
          "cod" -> map.filter(_._1.startsWith("cod_")).head._2,
          "nome" -> map.filter(_._1.startsWith("nome_")).head._2,
          "descr" -> map.filter(_._1.startsWith("descr_")).head._2)
      }
      .map(ModelAdapter.fromMap[CP2011Item](_))

  }

  def toJSONStream(filename: String) = {

    data(filename)
      .map(JSON.writer.writeValueAsString(_))

  }

}

case class CP2011Item(cod: String, nome: String, descr: String)

