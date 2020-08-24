package simples.refactorization

import org.elasticsearch.common.settings.Settings
import org.elasticsearch.node.InternalSettingsPreparer
import java.util.Collection
import org.elasticsearch.transport.Netty4Plugin
import org.elasticsearch.index.reindex.ReindexPlugin
import org.elasticsearch.plugins.Plugin
import org.elasticsearch.analysis.common.CommonAnalysisPlugin
import org.elasticsearch.node.Node
import java.util.Collections
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import java.nio.file.Paths
import com.typesafe.config.ConfigFactory
import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object EmbeddedNode {

  def fromConfigFile(conf_file: String) = {

    val _file = Paths.get(conf_file).toAbsolutePath().normalize().toFile()

    // load hocon
    val settings_content = ConfigFactory.empty()
      .withFallback(ConfigFactory.parseFileAnySyntax(_file))

    val settings = settings_content.getConfig("elasticsearch").entrySet()
      .foldRight(Settings.builder())((e, builder) => builder.put(e.getKey, e.getValue.unwrapped().toString()))
      .build()

    new EmbeddedNode(settings)

  }

}

class EmbeddedNode(settings: Settings) {

  import scala.concurrent.ExecutionContext.Implicits._

  val env = InternalSettingsPreparer.prepareEnvironment(settings, Collections.emptyMap(), null, null)

  // REVIEW the plugins!
  val plugins: Collection[Class[_ <: Plugin]] = List(
    classOf[Netty4Plugin],
    classOf[ReindexPlugin],
    classOf[CommonAnalysisPlugin])

  val node = new Node(env, plugins, true) {
    // DO NOTHING HERE! :-)
    // this is just a workaround needed to load plugins...
  }

  def start() = {

    Await.ready(Future { node.start() }, Duration.Inf)

  }

  def stop() = {

    if (!node.isClosed()) {
      node.close()
      node.awaitClose(60, TimeUnit.SECONDS)
    }

  }

}