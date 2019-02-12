package examples.istat

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import simples.utilities.JSON
import csv.CSVParser
import java.nio.file.Paths
import simples.ES
import scala.util.Success
import scala.util.Failure
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest

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

