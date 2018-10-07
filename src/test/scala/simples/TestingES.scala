

package simples

import org.junit.Before
import org.junit.After
import org.junit.Test
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits._
import com.fasterxml.jackson.databind.ObjectMapper
import scala.util.Try
import org.junit.Assert
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.io.Codec.string2codec

class TestingES {

  val json_mapper = new ObjectMapper
  val json_reader = json_mapper.reader()
  val json_writer = json_mapper.writerWithDefaultPrettyPrinter()

  val (_index, _type) = ("series", "got")

  val es = ES.remote

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

