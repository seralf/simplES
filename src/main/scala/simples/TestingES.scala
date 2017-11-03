package simples

import org.junit.Before
import examples.GOT_DATA
import org.junit.After
import org.junit.Test
import scala.io.Source

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.TypeRef
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.elasticsearch.common.settings.Settings
import java.net.InetAddress
import org.elasticsearch.common.transport.InetSocketTransportAddress
import scala.util.Try
import org.junit.Assert
import org.slf4j.LoggerFactory
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.action.update.UpdateRequest

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.elasticsearch.index.query.QueryBuilders

object ESRemote {

  val logger = LoggerFactory.getLogger(this.getClass)

  val client = new PreBuiltTransportClient(Settings.EMPTY)

  val hosts = Map("localhost" -> 9300)

  def start() {

    hosts.foreach { host =>
      client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host._1), host._2))
    }

  }

  def stop() {
    client.close()
  }

  object documents {

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

}

class TestingES {

  val json_mapper = new ObjectMapper
  val json_reader = json_mapper.reader()
  val json_writer = json_mapper.writerWithDefaultPrettyPrinter()

  val (_index, _type) = ("series", "got")

  val es = ESRemote

  @Before
  def before() {
    es.start()
  }

  @After
  def after() {
    //    es.stop()
  }

  @Test
  def testing_get() {

    //    "http://localhost:9200/series/got/4985/_source"

    val _id = "4985"
    val doc = es.documents.get(_index, _type, _id).get

    val json = json_writer.writeValueAsString(doc)
    val tree = json_reader.readTree(json)

    Assert.assertEquals("Oathkeeper", tree.at("/name").asText())
    Assert.assertEquals("2014-04-27", tree.at("/airdate").asText())

  }

  @Test
  def testing_add_remove() {

    val size_before = es.documents.size(_index, _type).get
    println("SIZE BEFORE: " + size_before)

    Assert.assertEquals(67, size_before)

    val _id = es.documents.add(_index, _type)("""{ "msg": "hello" }""").get
    println("ADDED DOC " + _id)

    val size_after_add = es.documents.size(_index, _type).get
    Assert.assertEquals(68, size_after_add)

    val _idr = es.documents.delete(_index, _type, _id).get
    println("DELETED DOC " + _idr)

    Assert.assertEquals(_id, _idr)

    val size_after = es.documents.size(_index, _type).get
    Assert.assertEquals(67, size_after)

  }

  def read_url(url: String) = Try {
    val src = Source.fromURL(url)("UTF-8")
    val txt = src.getLines().mkString("\n")
    src.close()
    txt
  }

}

