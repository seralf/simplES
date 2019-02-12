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

object MainMockES extends App {

  val (_index) = "twitter"

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

  // create index with settings and mapping
  if (!index_exists) {
    client.admin().indices().prepareCreate(_index)
      .setSettings("""
        index.number_of_shards: 1
        index.number_of_replicas: 0
      """, XContentType.YAML)
      .addMapping("_doc", """
       {
       	"_doc": {
       		"properties": {
       			"message": {
       				"type": "text",
       				"fields": {
       					"keyword": {
       						"type": "keyword",
       						"ignore_above": 256
       					}
       				}
       			},
       			"postDate": {
       				"type": "date"
       			},
       			"title": {
       				"type": "text",
       				"analyzer": "english"
       			},
       			"user": {
       				"type": "text",
       				"fields": {
       					"keyword": {
       						"type": "keyword",
       						"ignore_above": 256
       					}
       				}
       			}
       		}
       	}
       }  
      """, XContentType.JSON)
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

  for { i <- 0 to 40 } {

    val _id = String.format("%04d", Integer.valueOf(i.toString))

    bulkRequest.add(client.prepareIndex("twitter", "_doc", i.toString())
      .setSource(jsonBuilder()
        .startObject()
        .field("user", "kimchy")
        .field("postDate", new Date())
        .field("message", "trying out Elasticsearch")
        .endObject()))
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