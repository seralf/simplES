package simples

import com.typesafe.config.ConfigFactory
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.TransportAddress
import java.net.InetAddress
import java.nio.file.Paths
import simples.refactorization.EmbeddedNode

object ESHelper {

  import scala.collection.JavaConversions._
  import scala.collection.JavaConverters._

  def remote(filename: String): ESRemote = {

    val _file = Paths.get(filename).toAbsolutePath().normalize().toFile()

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

    new ESRemote(client)

  }

  def local(filename: String): ESLocal = {

    val _file = Paths.get(filename).toAbsolutePath().normalize().toFile()

    //    // load hocon
    //    val settings_content = ConfigFactory.empty()
    //      .withFallback(ConfigFactory.parseFileAnySyntax(_file))
    //
    //    // load ES config from hocon
    //    val settings = settings_content.getConfig("elasticsearch").entrySet()
    //      .foldRight(Settings.builder())((e, builder) => builder.put(e.getKey, e.getValue.unwrapped().toString()))
    //      .build()

    val embedded = EmbeddedNode.fromConfigFile(filename)
    new ESLocal(embedded.node.client(), embedded.node)

  }

}