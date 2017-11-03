package examples

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object GOT_DATA {

  val url = new URL("http://api.tvmaze.com/singlesearch/shows?q=game-of-thrones&embed=episodes")

  val json_mapper = new ObjectMapper
  val json_tree = json_mapper.reader().readTree(url.openStream())
  val json_writer = json_mapper.writerWithDefaultPrettyPrinter()

  def episodes = json_tree.get("_embedded").get("episodes").toList
    .map { ep =>
      (ep.get("id").asText(), json_writer.writeValueAsString(ep))
    }

}
