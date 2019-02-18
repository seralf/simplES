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
import utilities.JSON
import scala.util.Try
import org.elasticsearch.client.transport.TransportClient
import scala.concurrent.Await
import org.elasticsearch.action.bulk.BulkRequestBuilder
import org.elasticsearch.action.index.IndexRequest
import java.util.concurrent.TimeUnit
import java.io.FileInputStream
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._
import com.typesafe.config.ConfigRenderOptions
import java.io.File
import org.elasticsearch.client.Client

object ESHelper {

  def fromFile(_name: String) = Try {
    val src = Source.fromFile(_name)("UTF-8")
    val txt = src.getLines().mkString("\n")
    src.close()
    txt
  }

  def remote(filename: String): ESRemote = {

    val _file = Paths.get(filename).toAbsolutePath().normalize().toFile()

    // load hocon
    val settings_content = ConfigFactory.empty()
      .withFallback(ConfigFactory.parseFileAnySyntax(_file))

    // load ES config from hocon
    val settings = settings_content.getConfig("elasticsearch").entrySet()
      .foldRight(Settings.builder())((e, builder) => builder.put(e.getKey, e.getValue.unwrapped().toString()))
      .build()

    // initialize transport client
    val client = new PreBuiltTransportClient(settings)
    settings_content.getStringList("remote.hosts")
      .map(_.trim().split(":"))
      .foreach(e => client.addTransportAddress(new TransportAddress(InetAddress.getByName(e(0)), Integer.valueOf(e(1)))))

    new ESRemote(client)

  }

  def local(filename: String): ESLocal = {

    val _file = Paths.get(filename).toAbsolutePath().normalize().toFile()

    // load hocon
    val settings_content = ConfigFactory.empty()
      .withFallback(ConfigFactory.parseFileAnySyntax(_file))

    // load ES config from hocon
    val settings = settings_content.getConfig("elasticsearch").entrySet()
      .foldRight(Settings.builder())((e, builder) => builder.put(e.getKey, e.getValue.unwrapped().toString()))
      .build()

    val node = EmbeddedNode(settings)
    val client = node.client()

    new ESLocal(client, node)

  }

}

// ---------------------------------------------------------------------

class ES(client: Client) {

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

  protected var bulkProcessor: BulkProcessor = null

  def bulkProcessorInitialize() = BulkProcessor.builder(
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

    println("\n\n#### START BulkProcessor")
    bulkProcessor = bulkProcessorInitialize()
    
  }

  def stop() = Try {

    bulkProcessor.flush()
    bulkProcessor.close()
    // TODO: bulkProcessor.awaitClose(2, TimeUnit.SECONDS)

    // on shutdown
    Thread.sleep(2000)
    client.close()

  }

  def mapping_read(_mapping_path: String): Try[String] =
    ESHelper.fromFile(_mapping_path)

  def settings_read(_settings_path: String): Try[String] =
    ESHelper.fromFile(_settings_path)

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
    while (!index_exists(_index).get) {
      println(s"creating index ${_index}")
      client.admin().indices().prepareCreate(_index)
        .setSettings(settings_read(_settings).get, XContentType.JSON)
        .addMapping(_doc, mapping_read(_mapping).get, XContentType.JSON)
        .get()
      refresh(_index).get
      Thread.sleep(100)
    }
  }

  def refresh(_index: String) = Try {

    // refresh index
    client.admin().indices().prepareRefresh(_index).get()

    // prepare for search
    client.prepareSearch().get()

  }

  def indexing(_index: String, _type: String, _id: String = null, _source: String) {

    val req = new IndexRequest(_index, _type, _id).source(_source, XContentType.JSON)
    bulkProcessor.add(req)

  }

  // multiple indexing
  def indexing(_index: String, _type: String, docs: (String, String)*) {

    docs.toStream
      .foreach {
        case (_id, _source) =>
          indexing(_index, _type, _id, _source)
      }

    bulkProcessor.flush()

  }

  // REVIEW (from previous versions)
  def search(query: String): Seq[Any] = ???

}

// ---------------------------------------------------------------------

//class ES2(client: Client) {
//
//  import org.elasticsearch.common.xcontent.XContentFactory._
//  import org.elasticsearch.action.bulk.BackoffPolicy
//  import org.elasticsearch.action.bulk.BulkProcessor
//  import org.elasticsearch.common.unit.ByteSizeUnit
//  import org.elasticsearch.common.unit.ByteSizeValue
//  import org.elasticsearch.common.unit.TimeValue
//
//  val bulk_listener = new BulkProcessor.Listener() {
//
//    override def beforeBulk(executionId: Long, request: BulkRequest) {
//      println(s"ADD s${executionId}")
//    }
//
//    override def afterBulk(executionId: Long, request: BulkRequest, response: BulkResponse) {}
//
//    override def afterBulk(executionId: Long, request: BulkRequest, failure: Throwable) {}
//
//  }
//
//  // check available cores?
//  val cores = Runtime.getRuntime().availableProcessors() - 1
//  println(s"ES> concurrent requests? ${cores}")
//
//  def bulkProcessor = BulkProcessor.builder(
//    client,
//    bulk_listener)
//    .setConcurrentRequests(cores)
//    .setBulkActions(100)
//    .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
//    .setFlushInterval(TimeValue.timeValueSeconds(5))
//    .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(1000), 3))
//    .build()
//
//  def start() = Try {
//
//    // TODO: status check
//
//    println("\n\n#### START BulkProcessor")
//
//  }
//
//  def stop() = Try {
//
//    bulkProcessor.flush()
//    bulkProcessor.close()
//    // TODO: bulkProcessor.awaitClose(2, TimeUnit.SECONDS)
//
//    // on shutdown
//    Thread.sleep(2000)
//    client.close()
//
//  }
//
//  def mapping_read(_mapping_path: String): Try[String] =
//    ESHelper.fromFile(_mapping_path)
//
//  def settings_read(_settings_path: String): Try[String] =
//    ESHelper.fromFile(_settings_path)
//
//  def index_exists(_index: String) = Try {
//
//    println(s"\n\n\n\nES> check ${_index} .................................")
//
//    val ok = client.admin().indices().prepareExists(_index).get.isExists()
//
//    println(s"\n\n\n\nES> check ${_index} exists? ${ok}")
//
//    ok
//  }
//
//  // delete index
//  def index_delete(_index: String) = Try {
//
//    if (index_exists(_index).get)
//      client.admin().indices().prepareDelete(_index).get
//
//  }
//
//  // create index with settings and mapping
//  def index_create(_index: String, _doc: String)(_settings: String, _mapping: String) = Try {
//    if (!index_exists(_index).get) {
//      client.admin().indices().prepareCreate(_index)
//        .setSettings(settings_read(_settings).get, XContentType.JSON)
//        .addMapping(_doc, mapping_read(_mapping).get, XContentType.JSON)
//        .get()
//    }
//    refresh(_index).get
//  }
//
//  def refresh(_index: String) = Try {
//
//    // refresh index
//    client.admin().indices().prepareRefresh(_index).get()
//
//    // prepare for search
//    client.prepareSearch().get()
//
//  }
//
//  def indexing(_index: String, _type: String, _id: String = null, _source: String) {
//
//    val req = new IndexRequest(_index, _type, _id).source(_source, XContentType.JSON)
//    bulkProcessor.add(req)
//
//  }
//
//  // multiple indexing
//  def indexing(_index: String, _type: String, docs: (String, String)*) {
//
//    docs.toStream
//      .foreach {
//        case (_id, _source) =>
//          indexing(_index, _type, _id, _source)
//      }
//
//    bulkProcessor.flush()
//
//  }
//
//  // REVIEW (from previous versions)
//  def search(query: String): Seq[Any] = ???
//
//}