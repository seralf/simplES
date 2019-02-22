package simples

import java.nio.file.Paths
import com.typesafe.config.ConfigFactory
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.elasticsearch.common.transport.TransportAddress
import java.net.InetAddress
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.QueryBuilder
import org.apache.lucene.queryparser.xml.QueryBuilderFactory
import org.elasticsearch.search.builder.SearchSourceBuilder
import java.util.Collections
import org.elasticsearch.search.SearchModule
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.action.search.SearchAction
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils

object MainSearch extends App {

  import scala.collection.JavaConversions._
  import scala.collection.JavaConverters._

  val _file = Paths.get("src/main/resources/conf/es.conf").toAbsolutePath().normalize().toFile()

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

  val q_all = """
  {
  	"query": {
  		"match_all": {}
  	}
  }  
  """

  val q_2 = """
  
{
  "query": {
    "dis_max": {
      "queries": [
        { "match": { "nome": { "query": "programmatore" , "boost": 2 }  }},
        { "match": { "descr": { "query": "programmatore" , "boost": 0.1 } }}
      ],
      "tie_breaker": 0.3
    }
  }
}
    
  """

  val _query = q_2

  // ---- paring JSON query ----
  val searchSourceBuilder = new SearchSourceBuilder();
  val searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList())
  val parser = XContentFactory.xContent(XContentType.JSON).createParser(new NamedXContentRegistry(searchModule.getNamedXContents()), null, _query)
  searchSourceBuilder.parseXContent(parser, true)
  val searchRequestBuilder = new SearchRequestBuilder(client, SearchAction.INSTANCE)
  val response = searchRequestBuilder.setSource(searchSourceBuilder).get

  //  val response = client.prepareSearch("cp2011")
  //    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
  //    .setQuery(_query)
  //    .get()

  //  SearchResponse response = client.prepareSearch("index1", "index2")
  //        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
  //        .setQuery(QueryBuilders.termQuery("multi", "test"))                 // Query
  //        .setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
  //        .setFrom(0).setSize(60).setExplain(true)
  //        .get();

  response.getHits().toStream
    .zipWithIndex
    .foreach {
      case (hit, i) =>
        println(s"DOC [$i] ")
        println(hit)
    }

}