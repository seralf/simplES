package simples

import org.elasticsearch.client.Client
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.bulk.BulkRequest
import java.util.concurrent.TimeUnit
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.common.xcontent.XContentType
import scala.util.Try
import org.slf4j.LoggerFactory
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.action.update.UpdateRequest

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.typesafe.config.ConfigFactory

object ES {

  def remote = new ESRemote(ConfigFactory.empty())

  def local = new ESLocal(ConfigFactory.empty())

}

trait ES {

  val logger = LoggerFactory.getLogger(this.getClass)

  protected val client: Client = null

  def indexer = ESIndexer(client)

  def documents = ESDocumentHandler(client)

  def start() = Try {
    logger.debug("ES STARTING")
  }

  def stop() = Try {
    logger.debug("ES STOPPING")
    client.close()
  }

}

object ESDocumentHandler {
  def apply(client: Client) = new ESDocumentHandler(client)
}

protected class ESDocumentHandler(client: Client) {

  val logger = LoggerFactory.getLogger(this.getClass)

  def get(_index: String, _type: String, _id: String) = Try {

    val request = client.prepareGet(_index, _type, _id)
      .setOperationThreaded(true)

    val es_dsl = s"""
      GET ${request.request().index()}/${request.request().`type`()}/${request.request().id()}
    """.trim()

    logger.debug("ES: " + es_dsl)

    request.get()
      .getSource

  }

  def add(_index: String, _type: String, _id: String = null)(_source: String) = Try {
    client.prepareIndex(_index: String, _type: String)
      .setSource(_source, XContentType.JSON)
      .get()
      .getId
  }

  def delete(_index: String, _type: String, _id: String) = Try {
    client.prepareDelete(_index, _type, _id)
      .get()
      .getId
  }

  def update(_index: String, _type: String, _id: String = null)(_source: String) = Try {
    client.update(new UpdateRequest(_index, _type, _id).doc(_source, XContentType.JSON))
      .get()
      .getId
  }

  def refresh() = Try {
    client.admin().indices().prepareRefresh().execute().actionGet()
  }

  def size(_index: String, _type: String) = Try {

    refresh()

    client.prepareSearch(_index)
      .setTypes(_type)
      .setSize(0)
      .setQuery(QueryBuilders.queryStringQuery("""*:*"""))
      .get()
      .getHits()
      .getTotalHits()
  }

  def all() = Try {

    // MatchAll on the whole cluster with all default options
    client.prepareSearch()
      .get()
      .getHits
      .iterator().toStream
      .map { hit =>
        val _id = hit.docId()
        val fields = hit.getFields.map { f =>
          (f._2.getName, f._2.getValues)
        }
        Map("_id" -> _id) ++ fields
      }

  }

}

object ESIndexer {
  def apply(client: Client) = new ESIndexer(client)
}

protected class ESIndexer(client: Client) {

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
