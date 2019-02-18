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

object MainESLocal extends App {

  val es = new ESLocal
  es.start()

}

class ESLocal {

  val settings = Settings.builder()
    .put("cluster.name", "elasticsearch")
    .put("path.home", "target/ES")
    .put("http.enabled", "true")
    .put("http.host", "127.0.0.1")
    .put("transport.host", "0.0.0.0")
    .put("client.transport.sniff", true)
    .build

  val node = EmbeddedNode.create(settings)

  val client = node.client()

  def start() = Try {

    val thread = new Thread("es-embedded-node") {
      override def run() {
        println("run by: " + getName())
        node.start()
        Thread.sleep(Long.MaxValue)
      }
    };

    thread.start()

  }

  def stop() = Try {
    client.close()
    if (!node.isClosed()) {
      node.close()
    }
  }

}