package simples

import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.elasticsearch.common.settings.Settings
import java.net.InetAddress
import org.elasticsearch.common.transport.TransportAddress
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.common.xcontent.XContentBuilder
import java.util.Date
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse

import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.elasticsearch.common.settings.Settings
import java.net.InetAddress
import org.elasticsearch.common.transport.TransportAddress
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.common.xcontent.XContentBuilder
import java.util.Date
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import java.nio.file.Files
import java.nio.file.Paths
import scala.io.Source
import java.io.InputStream
import examples.istat.CP2011
import utilities.JSON

object ES extends App {

  val (_index, _doc) = ("cp2011", "doc")

  val settings = Settings.builder()
    .put("cluster.name", "elasticsearch")
    .put("client.transport.sniff", true)
    .put("client.transport.ping_timeout", "5s")
    .put("node.name", "es-client-mock")
    .build()

  val client = new PreBuiltTransportClient(settings)
    .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300))
    .addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"), 9300))

  // -------------------------------------------------------------------

  def index_exists = client.admin().indices().prepareExists(_index).get.isExists()

  // delete index
  if (index_exists)
    client.admin().indices().prepareDelete(_index).get

  // create index simply
  //  if (!index_exists)
  //    client.admin().indices().prepareCreate(_index)
  //      .get()

  def readFile(is: InputStream) = {

    val src = Source.fromInputStream(is)("UTF-8")
    val txt = src.getLines().mkString("\n")
    src.close()

    txt

  }

  val _mapping = readFile(this.getClass.getClassLoader.getResourceAsStream("data/ISTAT/_mapping.json"))

  val _settings = readFile(this.getClass.getClassLoader.getResourceAsStream("data/ISTAT/_settings.json"))

  // create index with settings and mapping
  if (!index_exists) {
    client.admin().indices().prepareCreate(_index)
      .setSettings(_settings, XContentType.JSON)
      .addMapping(_doc, _mapping, XContentType.JSON)
      .get()

    refresh
  }

  // refresh index
  def refresh = client.admin().indices()
    .prepareRefresh(_index)
    .get()

  // -------------------------------------------------------------------

  // EXAMPLE: bulk indexing

  import org.elasticsearch.common.xcontent.XContentFactory._

  import org.elasticsearch.action.bulk.BackoffPolicy
  import org.elasticsearch.action.bulk.BulkProcessor
  import org.elasticsearch.common.unit.ByteSizeUnit
  import org.elasticsearch.common.unit.ByteSizeValue
  import org.elasticsearch.common.unit.TimeValue

  val bulk_listener = new BulkProcessor.Listener() {

    override def beforeBulk(executionId: Long, request: BulkRequest) {}

    override def afterBulk(executionId: Long, request: BulkRequest, response: BulkResponse) {}

    override def afterBulk(executionId: Long, request: BulkRequest, failure: Throwable) {}

  }

  val bulkProcessor = BulkProcessor.builder(client, bulk_listener)
    .setBulkActions(10000)
    .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
    .setFlushInterval(TimeValue.timeValueSeconds(5))
    .setConcurrentRequests(10)
    .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
    .build()

  val bulkRequest = client.prepareBulk()

  CP2011.data
    .zipWithIndex
    .foreach {
      case (doc, i) =>

        val _id = String.format("%04d", Integer.valueOf(i.toString))

        val _source = JSON.writer.writeValueAsString(doc)

        bulkRequest.add(client.prepareIndex(_index, _doc, i.toString())
          .setSource(_source, XContentType.JSON))

    }

  val bulkResponse = bulkRequest.get()
  if (bulkResponse.hasFailures()) {
    // process failures by iterating through each bulk response item
  }

  // -------------------------------------------------------------------

  // on shutdown
  Thread.sleep(2000)
  client.close()

}