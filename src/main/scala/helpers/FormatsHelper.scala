package helpers

package helpers

import scala.util.Try
import java.net.URL
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.JsonNode
import java.net.URI
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory
import javax.xml.xpath.XPathConstants
import org.w3c.dom.NodeList
import scala.reflect.ClassTag
import javax.xml.namespace.QName

object FormatsHelper {

  val json_mapper = new ObjectMapper
  val json_reader = json_mapper.reader()
  val json_writer = json_mapper.writerWithDefaultPrettyPrinter()
    .withFeatures(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)

  val xml_module = new JacksonXmlModule()
  xml_module.setDefaultUseWrapper(false)
  val xml_mapper = new XmlMapper(xml_module)
  xml_mapper.enable(SerializationFeature.INDENT_OUTPUT)
  val xml_writer = xml_mapper.writerWithDefaultPrettyPrinter()

  val xml_builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
  val xpath = XPathFactory.newInstance().newXPath()

  def json_to_xml(json_tree: JsonNode, root: String) = Try {
    xml_writer.withRootName(root)
      .writeValueAsString(json_tree)
  }

}