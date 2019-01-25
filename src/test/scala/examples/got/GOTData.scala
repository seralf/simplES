package examples.got

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import simples.utilities.JSON

object GOT_DATA {

  //  SEE
  //  val url = new URL("https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22Rome%2C%20it%22)&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys")
  //  $.query.results.channel.item.forecast

  def episodes_local = {
    val is = this.getClass.getClassLoader.getResourceAsStream("data/GOT/got_episodes.json")
    val json_tree = JSON.reader.readTree(is)
    is.close()
    json_tree.get("_embedded").get("episodes").toList
      .map { ep =>
        (ep.get("id").asText(), JSON.writer.writeValueAsString(ep))
      }
  }

  def episodes_remote = {
    val url = new URL("http://api.tvmaze.com/singlesearch/shows?q=game-of-thrones&embed=episodes")
    val json_tree = JSON.reader.readTree(url.openStream())
    json_tree.get("_embedded").get("episodes").toList
      .map { ep =>
        (ep.get("id").asText(), JSON.writer.writeValueAsString(ep))
      }
  }

}

object MainGOTData extends App {

  GOT_DATA.episodes_local
    .zipWithIndex
    .foreach {
      case (item, i) =>
        println(i + ": " + item)
    }

}
