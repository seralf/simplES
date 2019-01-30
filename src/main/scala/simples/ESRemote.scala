package simples

import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.index.query.QueryBuilders

import org.slf4j.LoggerFactory

import java.net.InetAddress

import scala.util.Try
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

class ESRemote(config: Config) extends ES {

  override val client = new PreBuiltTransportClient(Settings.EMPTY)

  val hosts = Map(
    "localhost" -> 9300,
    "127.0.0.1" -> 9300)

  override def start() = Try {

    hosts.foreach { host =>
      client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host._1), host._2))
    }

  }

  override def stop() = Try {
    client.close()
  }

}

object MainESRemote extends App {

  val es = new ESRemote(ConfigFactory.empty())
  es.start()

  val (_index, _type) = ("series", "got")
  val size = es.documents.size(_index, _type).get

  println("SIZE? " + size)

}