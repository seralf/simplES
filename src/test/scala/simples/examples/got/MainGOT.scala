package simples.examples.got

import helpers.FormatsHelper
import simples.embedded.EmbeddedNode
import simples.ES
import java.io.FileInputStream
import javax.xml.xpath.XPathConstants
import org.w3c.dom.NodeList

import com.fasterxml.jackson.core.`type`.TypeReference
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import java.util.{ Map => JMap }

object MainGOT extends App {

  val episodes = GOT.loadData()

  val conf_file_path = "src/main/resources/conf/es-embedded.conf"

  val embedded = EmbeddedNode.withConfigFile(conf_file_path)
  embedded.start().get

  val es = ES.remote(conf_file_path)

  es.start().get

  episodes
    .zipWithIndex
    .foreach {
      case (map, i) =>

        println("ES> indexing: " + (i + 1))
        println(map)

        val _id = String.format("episode_%03d", Int.box(i + 1))
        val doc = FormatsHelper.json_writer.writeValueAsString(map)

        es.indexing("got", "episodes", _id, doc)

    }

  Thread.sleep(600000)

  es.stop().get

  embedded.stop().get

}

object GOT {

  def loadData(): Iterator[JMap[String, Object]] = {

    val fis = new FileInputStream("src/main/resources/data/GOT/episodes.json")
    val json_tree = FormatsHelper.json_reader.readTree(fis)
    fis.close()

    val xml_tree = FormatsHelper.json_to_xml(json_tree, "root").get

    val episodes_nl = FormatsHelper.xpath.compile("//episodes")
      .evaluate(xml_tree, XPathConstants.NODESET)
      .asInstanceOf[NodeList]

    val nodes = (for (i <- 0 to episodes_nl.getLength - 1) yield episodes_nl.item(i))
      .toList
      .zipWithIndex
      .map {
        case (node_xml, i) =>
          val node_json = FormatsHelper.xml_to_json(node_xml)
          val map = FormatsHelper.json_mapper.convertValue(node_json, new TypeReference[JMap[String, Object]]() {})
          println(map)
          map
      }.iterator

    nodes

  }

}