package simples

import java.util.concurrent.TimeUnit
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.action.index.IndexRequest
import scala.util.Try
import org.elasticsearch.client.Client
import org.slf4j.LoggerFactory
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse

/**
 * A simple object for handling bulk loading, using the current configurated node (remote, or embedded).
 */
object ESIndexer {
  def apply(client: Client) = new ESIndexer(client)
}

protected class ESIndexer(client: Client) {

  import org.elasticsearch.action.bulk.BackoffPolicy;
  import org.elasticsearch.action.bulk.BulkProcessor;
  import org.elasticsearch.common.unit.ByteSizeUnit;
  import org.elasticsearch.common.unit.ByteSizeValue;
  import org.elasticsearch.common.unit.TimeValue;

  val logger = LoggerFactory.getLogger(this.getClass)

  // TODO: externalization of config values.
  private val builder = BulkProcessor.builder(client, bulk_listener)
    .setBulkActions(100)
    .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
    .setFlushInterval(TimeValue.timeValueSeconds(0))
    .setConcurrentRequests(2)
    .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))

  private var bulk = builder.build()

  private def bulk_listener = new BulkProcessor.Listener() {

    override def beforeBulk(executionId: Long, request: BulkRequest) {}

    override def afterBulk(executionId: Long, request: BulkRequest, response: BulkResponse) {}

    override def afterBulk(executionId: Long, request: BulkRequest, failure: Throwable) {}

  }

  def start() = Try {
    bulk = builder.build()
  }

  // REVIEW HERE
  def index(_index: String, _type: String, _id: String = null)(_source: String = "{}") = Try {

    logger.debug(s"""indexing ${_index}/${_type}/${_id}""")

    val _request = new IndexRequest(_index, _type, _id)
      .source(_source, XContentType.JSON)

    //    bulk.flush()
    //    bulk.awaitClose(0, TimeUnit.MILLISECONDS) // HACK

  }

  def stop() = Try {
    bulk.flush()
    bulk.awaitClose(1, TimeUnit.SECONDS) // HACK
    bulk.close()
  }

  def await() = Try {
    bulk.flush()
    bulk.awaitClose(10, TimeUnit.MINUTES)
    client.admin().indices().prepareRefresh().get()
    client.prepareSearch().get()
  }

}
