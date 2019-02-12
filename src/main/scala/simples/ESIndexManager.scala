package simples

import org.elasticsearch.client.Client
import org.slf4j.LoggerFactory
import scala.util.Try
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest

object ESIndexManager {

  def apply(client: Client) = new ESIndexManager(client)
}

class ESIndexManager(client: Client) {

  val logger = LoggerFactory.getLogger(this.getClass)

  def drop(_index: String) = Try {

    //    val request = new DeleteIndexRequest(_index)

    if (client.admin().indices().prepareExists(_index).execute().actionGet().isExists()) {
      client.admin().indices().prepareDelete(_index).execute().actionGet()
    } else {
      logger.debug(s"ES> index ${_index} does not exists")
    }

  }

}