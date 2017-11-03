package simples

import org.elasticsearch.common.settings.Settings
import org.elasticsearch.node.InternalSettingsPreparer
import org.elasticsearch.transport.Netty4Plugin
import org.elasticsearch.plugins.Plugin

import java.util.Collection

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.elasticsearch.env.Environment
import org.elasticsearch.node.Node
import java.util.Collections

object EmbeddedNode {

  //  SEE: http://api.tvmaze.com/singlesearch/shows?q=game-of-thrones&embed=episodes

  def create(settings: Settings) = {
    val env = InternalSettingsPreparer.prepareEnvironment(settings, null)
    val plugins: Collection[Class[_ <: Plugin]] = List(classOf[Netty4Plugin])
    new EmbeddedNode(env, plugins)
  }

  class EmbeddedNode(env: Environment, plugins: Collection[Class[_ <: Plugin]] = Collections.emptyList()) extends Node(env, plugins)

  // MORE settings
  def MORE_SETTINGS = """
      cluster.name: elasticsearch
      node.name: embedded
      node.master: true
      node.data: true
      path.conf: target/ES/conf
      path.home: target/ES
      path.data: target/ES/data
      path.logs: target/ES/logs
      transport.type: local
      http.type: netty4
      http.enabled: true
    """

  // CHECK  val client = new org.elasticsearch.transport.client.PreBuiltTransportClient(settings)

}