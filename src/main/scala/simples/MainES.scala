package simples

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Properties
import org.elasticsearch.common.settings.Settings
import java.util.Arrays
import org.elasticsearch.node.Node
import org.elasticsearch.env.Environment
import java.util.Collection
import org.elasticsearch.plugins.Plugin
import java.util.Collections
import org.elasticsearch.node.InternalSettingsPreparer
import org.elasticsearch.common.network.NetworkModule
import org.elasticsearch.transport.Netty4Plugin
import org.apache.commons.codec.binary.StringUtils
import java.nio.file.Files
import org.elasticsearch.client.Client

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import java.util.concurrent.TimeUnit
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.common.xcontent.XContent
import scala.util.Try
import java.net.URL
import scala.io.Source
import examples.GOT_DATA

/**
 * CHECK: http://localhost:9200/series/got/_search
 */
object MainES extends App {

  val es = ES.local
  es.start()

  val (_index, _type) = ("series", "got")

  GOT_DATA.episodes
    .zipWithIndex
    .foreach {
      case (episode, idx) =>
        es.indexer.index(_index, _type, episode._1)(episode._2)
    }

}

object JSON {

  val json_mapper = new ObjectMapper
  val json_reader = json_mapper.reader()
  val json_writer = json_mapper.writerWithDefaultPrettyPrinter()

}