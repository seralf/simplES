//package simples
//
//import org.elasticsearch.common.settings.Settings
//import org.elasticsearch.node.InternalSettingsPreparer
//import java.util.Collection
//import org.elasticsearch.transport.Netty4Plugin
//import org.elasticsearch.index.reindex.ReindexPlugin
//import org.elasticsearch.plugins.Plugin
//import org.elasticsearch.analysis.common.CommonAnalysisPlugin
//import org.elasticsearch.node.Node
//import java.util.Collections
//import scala.collection.JavaConversions._
//import scala.collection.JavaConverters._
//
//object EmbeddedNode {
//
//  def apply(settings: Settings) = {
//
//    //    CHECK
//    //     Settings settings = InternalSettingsPreparer.prepareSettings(Settings.EMPTY);
//    //          Environment env = InternalSettingsPreparer.prepareEnvironment(baseEnvSettings, emptyMap(), null, () -> defaultNodeName);
//
//    val env = InternalSettingsPreparer.prepareEnvironment(settings, Collections.emptyMap(), null, null)
//
//    // REVIEW the plugins
//    val plugins: Collection[Class[_ <: Plugin]] = List(
//      classOf[Netty4Plugin],
//      classOf[ReindexPlugin],
//      classOf[CommonAnalysisPlugin])
//
//    //    new Node(env, plugins) {}
//
//    new Node(env, plugins, true) {
//      //      override def registerDerivedNodeNameWithLogger(name: String) {
//      //        println("Node.registerDerivedNodeNameWithLogger :: " + name)
//      //      }
//    }
//
//  }
//
//}
//
//
