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

  def create(settings: Settings) = {
    val env = InternalSettingsPreparer.prepareEnvironment(settings, null)
    val plugins: Collection[Class[_ <: Plugin]] = List(classOf[Netty4Plugin])
    new EmbeddedNode(env, plugins, false)
  }

  class EmbeddedNode(env: Environment, plugins: Collection[Class[_ <: Plugin]], forbidPrivateSettings: Boolean) extends Node(env, plugins, forbidPrivateSettings) {

    override def registerDerivedNodeNameWithLogger(name: String) {}

  }

}