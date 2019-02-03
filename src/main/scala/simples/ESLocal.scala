package simples

import org.elasticsearch.common.settings.Settings
import org.elasticsearch.client.Client
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.common.xcontent.XContentType
import java.util.concurrent.TimeUnit
import scala.util.Try
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config

class ESLocal(config: Config) extends ES {

  val settings = Settings.builder()
    .put("cluster.name", "elasticsearch")
    .put("path.home", "target/ES")
    .put("http.enabled", "true")
    //    .put("http.host", "0.0.0.0")
    //    .put("transport.host", "127.0.0.1")
    .put("http.host", "127.0.0.1")
    .put("transport.host", "0.0.0.0")
    .put("client.transport.sniff", true)
    .build

  val node = EmbeddedNode.create(settings)

  override val client = node.client()

  override def start() = Try {

    val thread = new Thread("other thread") {
      override def run() {
        println("run by: " + getName())
        node.start()
        Thread.sleep(Long.MaxValue)
      }
    };

    thread.start()

  }

  override def stop() = Try {
    client.close()
    if (!node.isClosed()) {
      node.close()
    }
  }

}
