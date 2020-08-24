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

import com.typesafe.config.ConfigRenderOptions
import java.io.File
import org.elasticsearch.client.Client
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.typesafe.config.Config

object ES {

  def remote(config_file_path: String): ES =
    new ES(ES.clientFromConfig(config_file_path))

  private def clientFromConfig(config_file_path: String): Client = {

    val _file = Paths.get(config_file_path).toAbsolutePath().normalize().toFile()

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

    client

  }

}

class ES(val client: Client) {

  import org.elasticsearch.common.xcontent.XContentFactory._
  import org.elasticsearch.action.bulk.BackoffPolicy
  import org.elasticsearch.action.bulk.BulkProcessor
  import org.elasticsearch.common.unit.ByteSizeUnit
  import org.elasticsearch.common.unit.ByteSizeValue
  import org.elasticsearch.common.unit.TimeValue

  protected val logger = LoggerFactory.getLogger(this.getClass)

  val bulk_listener = new BulkProcessor.Listener() {

    override def beforeBulk(executionId: Long, request: BulkRequest) {
      println(s"ADD s${executionId}")
    }

    override def afterBulk(executionId: Long, request: BulkRequest, response: BulkResponse) {}

    override def afterBulk(executionId: Long, request: BulkRequest, failure: Throwable) {}

  }

  val cores = Runtime.getRuntime().availableProcessors() + 1
  logger.debug(s"ES> concurrent requests? ${cores}")

  protected var bulkProcessor: BulkProcessor = null

  // TODO: externalize bulk configs!
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

    logger.debug("ES> START BulkProcessor")
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
    fromFile(_mapping_path)

  def settings_read(_settings_path: String): Try[String] =
    fromFile(_settings_path)

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

  // TODO:
  def search(query: String): Seq[Any] = ???

  //  // REVIEW (from previous versions)
  //  def search(query: String): Seq[Any] = {
  //
  //    //    val response = client.prepareSearch("index1", "index2")
  //    //      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
  //    //      .setQuery(QueryBuilders.termQuery("multi", "test")) // Query
  //    //      .setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18)) // Filter
  //    //      .setFrom(0).setSize(60).setExplain(true)
  //    //      .get();
  //
  //    ???
  //  }

  private def fromFile(_name: String) = Try {
    val src = Source.fromFile(_name)("UTF-8")
    val txt = src.getLines().mkString("\n")
    src.close()
    txt
  }

}

