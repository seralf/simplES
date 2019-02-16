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
import scala.util.Try
import org.elasticsearch.client.transport.TransportClient
import scala.concurrent.Await
import org.elasticsearch.action.bulk.BulkRequestBuilder
import org.elasticsearch.action.index.IndexRequest
import java.util.concurrent.TimeUnit

object MainES extends App {

  val (_index, _doc) = ("cp2011", "doc")

  val es = ESHelper.create()
  es.start()

  es.index_delete(_index)
  es.index_create(_index, _doc)("data/ISTAT/_settings.json", "data/ISTAT/_mapping.json")

  es.indexing_data_example()

  es.stop()

}

object ESHelper {

  def create() = {
    new ES(transport())
  }

  def transport() = {

    // caricare da file
    val settings = Settings.builder()
      .put("cluster.name", "elasticsearch")
      .put("client.transport.sniff", true)
      .put("client.transport.ping_timeout", "5s")
      .put("node.name", "es-client-mock")
      .build()

    val client = new PreBuiltTransportClient(settings)
      .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300))
      .addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"), 9300))

    client

  }

}

class ES(client: TransportClient) {

  import org.elasticsearch.common.xcontent.XContentFactory._
  import org.elasticsearch.action.bulk.BackoffPolicy
  import org.elasticsearch.action.bulk.BulkProcessor
  import org.elasticsearch.common.unit.ByteSizeUnit
  import org.elasticsearch.common.unit.ByteSizeValue
  import org.elasticsearch.common.unit.TimeValue

  val bulk_listener = new BulkProcessor.Listener() {

    override def beforeBulk(executionId: Long, request: BulkRequest) {
      println(s"ADD s${executionId}")
    }

    override def afterBulk(executionId: Long, request: BulkRequest, response: BulkResponse) {}

    override def afterBulk(executionId: Long, request: BulkRequest, failure: Throwable) {}

  }

  val cores = Runtime.getRuntime().availableProcessors() + 1
  println(s"ES> concurrent requests? ${cores}")

  val bulkProcessor = BulkProcessor.builder(
    client,
    bulk_listener)
    .setConcurrentRequests(cores)
    .setBulkActions(100)
    .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
    .setFlushInterval(TimeValue.timeValueSeconds(5))
    .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(1000), 3))
    .build()

  def start() = Try {
    // TODO: status check
  }

  def stop() = Try {

    bulkProcessor.flush()
    bulkProcessor.close()
    //    bulkProcessor.awaitClose(2, TimeUnit.SECONDS)

    // on shutdown
    Thread.sleep(2000)
    client.close()

  }

  def mapping_read(_mapping_path: String) =
    fromInputStream(fromResource(_mapping_path))

  def settings_read(_settings_path: String) =
    fromInputStream(fromResource(_settings_path))

  def index_exists(_index: String) = Try {
    client.admin().indices().prepareExists(_index).get.isExists()
  }

  // delete index
  def index_delete(_index: String) = Try {
    if (index_exists(_index).get)
      client.admin().indices().prepareDelete(_index).get
  }

  // create index with settings and mapping
  def index_create(_index: String, _doc: String)(_settings: String, _mapping: String) = Try {
    if (!index_exists(_index).get) {
      client.admin().indices().prepareCreate(_index)
        .setSettings(settings_read(_settings).get, XContentType.JSON)
        .addMapping(_doc, mapping_read(_mapping).get, XContentType.JSON)
        .get()
      refresh(_index).get
    }
  }

  def refresh(_index: String) = Try {

    // refresh index
    client.admin().indices().prepareRefresh(_index).get()

    // prepare for search
    client.prepareSearch().get()

  }

  def fromInputStream(is: InputStream) = Try {
    val src = Source.fromInputStream(is)("UTF-8")
    val txt = src.getLines().mkString("\n")
    src.close()
    txt
  }

  def fromResource(_name: String) = {
    this.getClass.getClassLoader.getResourceAsStream(_name)
  }

  // -------------------------------------------------------------------
  // bulk indexing

  def indexing_data_example() {

    val (_index, _doc) = ("cp2011", "doc")

    val bulkRequest: BulkRequestBuilder = client.prepareBulk()

    CP2011.data
      .zipWithIndex
      .foreach {
        case (doc, i) =>

          val _id = String.format("%04d", Integer.valueOf(i.toString))
          val _source = JSON.writer.writeValueAsString(doc)

          val req = new IndexRequest(_index, _doc, _id).source(_source, XContentType.JSON)
          bulkProcessor.add(req)

      }

    bulkProcessor.flush()

  }

}