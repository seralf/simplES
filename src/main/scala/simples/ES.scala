package simples

import org.elasticsearch.common.settings.Settings
import org.elasticsearch.client.Client
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.common.xcontent.XContentType
import java.util.concurrent.TimeUnit
import scala.util.Try

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
