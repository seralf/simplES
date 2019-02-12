package simples

import org.elasticsearch.client.Client
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.bulk.BulkRequest
import java.util.concurrent.TimeUnit
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.common.xcontent.XContentType
import scala.util.Try
import org.slf4j.LoggerFactory
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.action.update.UpdateRequest

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.typesafe.config.ConfigFactory
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest

object ES {

  def remote = new ESRemote(ConfigFactory.empty())

  def local = new ESLocal(ConfigFactory.empty())

}

trait ES {

  val logger = LoggerFactory.getLogger(this.getClass)

  protected val client: Client = null

  def indexer = ESIndexer(client)

  def documents = ESDocumentHandler(client)

  def index = ESIndexManager(client)

  def start() = Try {
    logger.debug("ES STARTING")
  }

  def stop() = Try {
    logger.debug("ES STOPPING")
    client.close()
  }

}

object ESDocumentHandler {
  def apply(client: Client) = new ESDocumentHandler(client)
}

protected class ESDocumentHandler(client: Client) {

  val logger = LoggerFactory.getLogger(this.getClass)

  def get(_index: String, _type: String, _id: String) = Try {

    val request = client.prepareGet(_index, _type, _id)
      .setOperationThreaded(true)

    val es_dsl = s"""
      GET ${request.request().index()}/${request.request().`type`()}/${request.request().id()}
    """.trim()

    logger.debug("ES: " + es_dsl)

    request.get()
      .getSource

  }

  def add(_index: String, _type: String, _id: String = null)(_source: String) = Try {
    client.prepareIndex(_index: String, _type: String)
      .setSource(_source, XContentType.JSON)
      .get()
      .getId
  }

  def delete(_index: String, _type: String, _id: String) = Try {
    client.prepareDelete(_index, _type, _id)
      .get()
      .getId
  }

  def update(_index: String, _type: String, _id: String = null)(_source: String) = Try {
    client.update(new UpdateRequest(_index, _type, _id).doc(_source, XContentType.JSON))
      .get()
      .getId
  }

  def refresh() = Try {
    client.admin().indices().prepareRefresh().execute().actionGet()
  }

  def search(_index: String, _type: String)(query: String = "*:*") = Try {

    refresh()

    client.prepareSearch(_index)
      .setTypes(_type)
      .setSize(0)
      .setQuery(QueryBuilders.queryStringQuery(query))
      .get
      .getHits
      .getHits
      .toStream
      .map { hit =>

        hit.getSourceAsMap

      }
  }

  def size(_index: String, _type: String) = Try {

    refresh()

    client.prepareSearch(_index)
      .setTypes(_type)
      .setSize(0)
      .setQuery(QueryBuilders.queryStringQuery("""*:*"""))
      .get()
      .getHits()
      .getTotalHits()
  }

  def all() = Try {

    // MatchAll on the whole cluster with all default options
    client.prepareSearch()
      .get()
      .getHits
      .iterator().toStream
      .map { hit =>
        val _id = hit.docId()
        val fields = hit.getFields.map { f =>
          (f._2.getName, f._2.getValues)
        }
        Map("_id" -> _id) ++ fields
      }

  }

}
