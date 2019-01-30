package examples.got

import simples.ES
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.index.query.QueryBuilders._
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.client.transport.TransportClient
import java.io.FileInputStream
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.xcontent.XContentType
import java.net.InetAddress

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object MainGOTSearch extends App {

  val settings = Settings.builder()
    //    .loadFromSource("conf/elasticsearch.yml", XContentType.YAML)
    .put("client.transport.sniff", true)
    .put("cluster.name", "elasticsearch")
    .put("node.name", "embedded-node")
    .build()

  val _client = new PreBuiltTransportClient(settings)
    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300))

  val ok = _client.connectedNodes().foreach(println(_))

  val response = _client.prepareSearch("series")
    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
    .setQuery(QueryBuilders.matchAllQuery())
    .setExplain(true)
    .get()

  response.getHits.getHits
    .zipWithIndex
    .foreach {
      case (hit, i) =>
        println(s"$i: ${hit.getId}")
        println(hit.getSource)
    }

  _client.close()

}