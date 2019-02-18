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

class ESLocal(client: Client, node: Node) extends ES(client)

class ESLocal2(client: Client, node: Node) extends ES(client) {

  val logger = LoggerFactory.getLogger(this.getClass)

  //  var thread: Thread = null

  override def start() = Try {

    super.start()

    val thread = new Thread("es-embedded-node") {
      override def run() {
        node.start()
        Thread.sleep(Long.MaxValue)
      }
    }

    //    if (node.isClosed())
    thread.start()

  }

  override def stop() = Try {

    if (!node.isClosed()) {

      // CHECK thread.stop()

      node.close()
    }

    super.stop()

  }

}

object EmbeddedNode {

  def apply(settings: Settings) = {
    val env = InternalSettingsPreparer.prepareEnvironment(settings, null)

    // REVIEW
    val plugins: Collection[Class[_ <: Plugin]] = List(
      classOf[Netty4Plugin],
      classOf[ReindexPlugin],
      classOf[CommonAnalysisPlugin])

    new EmbeddedNode(env, plugins, true)
  }

}

class EmbeddedNode(env: Environment, plugins: Collection[Class[_ <: Plugin]], forbidPrivateSettings: Boolean)
  extends Node(env, plugins, forbidPrivateSettings) {

  override def registerDerivedNodeNameWithLogger(name: String) {}

}
