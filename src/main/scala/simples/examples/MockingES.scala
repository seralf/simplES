package simples.examples

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import simples.ES
import simples.embedded.EmbeddedNode

object MockingES extends App {

  val conf_file_path = "src/main/resources/conf/es-embedded.conf"

  val embedded = EmbeddedNode.withConfigFile(conf_file_path)
  embedded.start().get

  val es = ES.remote(conf_file_path)

  es.start().get

  Thread.sleep(60000)

  es.stop().get

  embedded.stop().get

}