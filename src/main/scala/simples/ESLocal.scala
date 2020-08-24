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
import org.elasticsearch.node.InternalSettingsPreparer
import org.elasticsearch.transport.Netty4Plugin

import org.elasticsearch.plugins.Plugin
import org.elasticsearch.env.Environment
import org.elasticsearch.node.Node
import java.util.Collection

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import org.elasticsearch.index.reindex.ReindexPlugin
import org.elasticsearch.analysis.common.CommonAnalysisPlugin
import java.util.Collections
import java.util.function.Supplier

object MainESLocal extends App {

  val _file = Paths.get("src/main/resources/conf/es-local.conf").toAbsolutePath().normalize().toFile()

  // load hocon
  val settings_content = ConfigFactory.empty()
    .withFallback(ConfigFactory.parseFileAnySyntax(_file))
  val settings = settings_content.getConfig("elasticsearch").entrySet()
    .foldRight(Settings.builder())((e, builder) => builder.put(e.getKey, e.getValue.unwrapped().toString()))
    .build()

  val node = EmbeddedNode(settings)

  val es = new ESLocal(node.client(), node)
  es.start()
}

class ESLocal(client: Client, node: Node) extends ES(client) {

  override def start() = Try {

    node.start()
    Thread.sleep(500)

    super.start()

    logger.debug("ES> ESLocal started")

  }

  override def stop() = Try {

    if (!node.isClosed()) {

      // CHECK thread.stop()

      node.close()
    }

    super.stop()

    logger.debug("ES> ESLocal stopped")

  }

}

object EmbeddedNode {

  def apply(settings: Settings) = {

    //    CHECK
    //     Settings settings = InternalSettingsPreparer.prepareSettings(Settings.EMPTY);
    //          Environment env = InternalSettingsPreparer.prepareEnvironment(baseEnvSettings, emptyMap(), null, () -> defaultNodeName);

    val env = InternalSettingsPreparer.prepareEnvironment(settings, Collections.emptyMap(), null, null)

    // REVIEW the plugins
    val plugins: Collection[Class[_ <: Plugin]] = List(
      classOf[Netty4Plugin],
      classOf[ReindexPlugin],
      classOf[CommonAnalysisPlugin])

    //    new Node(env, plugins) {}

    new Node(env, plugins, true) {
//      override def registerDerivedNodeNameWithLogger(name: String) {
//        println("Node.registerDerivedNodeNameWithLogger :: " + name)
//      }
    }

  }

}


