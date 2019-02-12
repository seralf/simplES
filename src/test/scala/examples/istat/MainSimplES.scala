package examples.istat

import simples.ES
import simples.utilities.JSON
import scala.util.Success
import scala.util.Failure
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.elasticsearch.common.settings.Settings
import java.net.InetAddress
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.common.xcontent.XContentType
import simples.ESIndexer

object MainSimplES extends App {

  val (_index, _type) = ("taxonomy", "cp2011")

  val client = new PreBuiltTransportClient(Settings.EMPTY)
  client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getLocalHost, 9300))

  val indexer = ESIndexer(client)
  indexer.start()

  println("ES> indexing example data...")

  CP2011.data
    .zipWithIndex
    .foreach {
      case (doc, idx) =>

        println(s"ES> add doc n° ${idx}")

        val _id = doc.cod_5.replace(".", "-")
        val _source = JSON.writer.writeValueAsString(doc)

        indexer.index(_index, _type, _id)(_source)

    }

  indexer.stop()

}