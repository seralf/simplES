//package simples
//
//import java.nio.file.Paths
//import com.typesafe.config.ConfigFactory
//import scala.collection.JavaConverters._
//import scala.collection.JavaConversions._
//import org.elasticsearch.common.settings.Settings
//
//object MainEmbeddedNode extends App {
//
//  val _file = Paths.get("src/main/resources/conf/es-local.conf").toAbsolutePath().normalize().toFile()
//
//  // load hocon
//  val settings_content = ConfigFactory.empty()
//    .withFallback(ConfigFactory.parseFileAnySyntax(_file))
//  val settings = settings_content.getConfig("elasticsearch").entrySet()
//    .foldRight(Settings.builder())((e, builder) => builder.put(e.getKey, e.getValue.unwrapped().toString()))
//    .build()
//
//  val thread = new Thread("es-embedded-node") {
//    override def run() {
//      val node = EmbeddedNode(settings)
//      node.start()
//      Thread.sleep(Long.MaxValue)
//    }
//  }
//
//  //    if (node.isClosed())
//  thread.start()
//
//}