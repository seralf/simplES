package simples

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Properties
import org.elasticsearch.common.settings.Settings
import java.util.Arrays
import org.elasticsearch.node.Node
import org.elasticsearch.env.Environment
import java.util.Collection
import org.elasticsearch.plugins.Plugin
import java.util.Collections
import org.elasticsearch.node.InternalSettingsPreparer
import org.elasticsearch.common.network.NetworkModule
import org.elasticsearch.transport.Netty4Plugin
import org.apache.commons.codec.binary.StringUtils
import java.nio.file.Files
import org.elasticsearch.client.Client

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import java.util.concurrent.TimeUnit
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.common.xcontent.XContent
import scala.util.Try
import java.net.URL
import scala.io.Source

object MainES extends App {

  val es = ES
  es.start()

  val (_index, _type) = ("series", "got")

  GOT_DATA.episodes
    .zipWithIndex
    .foreach {
      case (episode, idx) =>
        es.indexer.index(_index, _type, episode._1)(episode._2)
    }

}

object ES {

  val settings = Settings.builder()
    .put("cluster.name", "elasticsearch")
    .put("path.home", "target/ES")
    .put("http.enabled", "true")
    .build

  val node = EmbeddedNode.create(settings)

  def start() {

    val thread = new Thread("other thread") {
      override def run() {
        println("run by: " + getName())
        node.start()
        Thread.sleep(Long.MaxValue)
      }
    };

    thread.start()

  }

  def stop() {
    if (!node.isClosed()) {
      node.close()
    }
  }

  def indexer = Indexer(node.client())

}

protected object Indexer {

  def apply(client: Client) = new ESIndexer(client)

  class ESIndexer(client: Client) {

    import org.elasticsearch.action.bulk.BackoffPolicy;
    import org.elasticsearch.action.bulk.BulkProcessor;
    import org.elasticsearch.common.unit.ByteSizeUnit;
    import org.elasticsearch.common.unit.ByteSizeValue;
    import org.elasticsearch.common.unit.TimeValue;

    private val builder = BulkProcessor.builder(client, bulk_listener)
      .setBulkActions(50)
      .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
      .setFlushInterval(TimeValue.timeValueSeconds(4))
      .setConcurrentRequests(2)
      .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))

    private var bulk = builder.build()

    private def bulk_listener = new BulkProcessor.Listener() {

      override def beforeBulk(executionId: Long, request: BulkRequest) {}

      override def afterBulk(executionId: Long, request: BulkRequest, response: BulkResponse) {}

      override def afterBulk(executionId: Long, request: BulkRequest, failure: Throwable) {}

    }

    def start() {
      bulk = builder.build()
    }

    def index(_index: String, _type: String, _id: String = null)(_source: String = "{}") = Try {

      println(s"""indexing ${_index}/${_type}/${_id}""")

      val _request = new IndexRequest(_index, _type, _id)
        .source(_source, XContentType.JSON)
      bulk.add(_request)
    }

    def stop {
      bulk.flush()
      bulk.close()
    }

    def await {
      bulk.flush()
      bulk.awaitClose(10, TimeUnit.MINUTES)
      client.admin().indices().prepareRefresh().get()
      client.prepareSearch().get()
    }

  }

}

object GOT_DATA {

  val url = new URL("http://api.tvmaze.com/singlesearch/shows?q=game-of-thrones&embed=episodes")

  val json_mapper = new ObjectMapper
  val json_tree = json_mapper.reader().readTree(url.openStream())
  val json_writer = json_mapper.writerWithDefaultPrettyPrinter()

  def episodes = json_tree.get("_embedded").get("episodes").toList
    .map { ep =>
      (ep.get("id").asText(), json_writer.writeValueAsString(ep))
    }

}

object EmbeddedNode {

  //  SEE: http://api.tvmaze.com/singlesearch/shows?q=game-of-thrones&embed=episodes

  def create(settings: Settings) = {
    val env = InternalSettingsPreparer.prepareEnvironment(settings, null)
    val plugins: Collection[Class[_ <: Plugin]] = List(classOf[Netty4Plugin])
    new EmbeddedNode(env, plugins)
  }

  class EmbeddedNode(env: Environment, plugins: Collection[Class[_ <: Plugin]] = Collections.emptyList()) extends Node(env, plugins)

  // MORE settings
  def MORE_SETTINGS = """
      cluster.name: elasticsearch
      node.name: embedded
      node.master: true
      node.data: true
      path.conf: target/ES/conf
      path.home: target/ES
      path.data: target/ES/data
      path.logs: target/ES/logs
      transport.type: local
      http.type: netty4
      http.enabled: true
    """

  // CHECK  val client = new org.elasticsearch.transport.client.PreBuiltTransportClient(settings)

}

object JSON {

  val json_mapper = new ObjectMapper
  val json_reader = json_mapper.reader()
  val json_writer = json_mapper.writerWithDefaultPrettyPrinter()

}