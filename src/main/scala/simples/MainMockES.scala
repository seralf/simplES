package simples

import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.elasticsearch.common.settings.Settings
import java.net.InetAddress
import org.elasticsearch.common.transport.TransportAddress
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.common.xcontent.XContentBuilder

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
      			"title": {
      				"type": "text",
      				"analyzer": "english"
      			}
      		}
      	}
      }  
      """, XContentType.JSON)
      .get()
  }

  // -------------------------------------------------------------------

  // -------------------------------------------------------------------

  // on shutdown
  Thread.sleep(2000)
  client.close()

}