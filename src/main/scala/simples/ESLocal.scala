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

class ESLocal(client: Client, node: Node) extends ES(client) {

  val logger = LoggerFactory.getLogger(this.getClass)

  //  var thread: Thread = null

  override def start() = Try {

    super.start()

    val thread = new Thread("es-embedded-node") {
      override def run() {
        node.start()
        Thread.sleep(Long.MaxValue)
      }
    };

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
    val plugins: Collection[Class[_ <: Plugin]] = List(classOf[Netty4Plugin])
    new EmbeddedNode(env, plugins, false)
  }

}

class EmbeddedNode(env: Environment, plugins: Collection[Class[_ <: Plugin]], forbidPrivateSettings: Boolean)
  extends Node(env, plugins, forbidPrivateSettings) {

  override def registerDerivedNodeNameWithLogger(name: String) {}

}
