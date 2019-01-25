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
      .parse[Map[String, _]]()
    //      .parse[CP2011Item]()

  }

  def toJSONStream = {

    data

      .map { item =>

        //        val json = JSON.writer.writeValueAsString(item)
//        println(item.getClass, item.toMap.get("nome_5"), item.toMap.get("﻿cod_5"))
        println(item.getClass, item.toMap.get("nome_5"), item.toMap.get("cod_5"))
        println(item.keys.mkString("|"))

        //        val id = item.get("cod_5").get.toString()
        //        val path = id.split(".")
        //        println("id", path.mkString("|"))

        item
      }

      .map { item =>

        //        if (!item.keys.contains("cod_5")) {
        //          System.err.println("WARNING")
        //          System.err.println(item)
        //          System.err.println(item.keys)
        //          System.err.println(item.getOrElse("cod_5", "NO"))
        //        }

        item
      }
      .map(JSON.writer.writeValueAsString(_))

  }

}

// CHECK: Map is missing required parameter (BUG: it seems to be always the first one!)
//case class CP2011Item(﻿cod_5: String, `nome_5`: String, `descr_5`: String)

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
