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
import javax.xml.transform.TransformerFactory
import org.w3c.dom.Document
import javax.xml.transform.dom.DOMSource
import org.w3c.dom.Node
import javax.xml.transform.stream.StreamResult
import java.io.ByteArrayOutputStream

object FormatsHelper {

  val json_mapper = new ObjectMapper
  val json_reader = json_mapper.reader()
  val json_writer = json_mapper.writerWithDefaultPrettyPrinter()
    .withFeatures(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)

  val xml_module = new JacksonXmlModule()
  xml_module.setDefaultUseWrapper(false)
  val xml_mapper = new XmlMapper(xml_module)
  xml_mapper.enable(SerializationFeature.INDENT_OUTPUT)
  val xml_reader = xml_mapper.reader()
  val xml_writer = xml_mapper.writerWithDefaultPrettyPrinter()

  val xml_builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
  val xpath = XPathFactory.newInstance().newXPath()

  def xml_writeAsString(doc: Node) = {
    val baos = new ByteArrayOutputStream
    val transformerFactory = TransformerFactory.newInstance()
    val transformer = transformerFactory.newTransformer()
    val domSource = new DOMSource(doc)
    val streamResult = new StreamResult(baos)
    transformer.transform(domSource, streamResult)
    baos.toString()
  }

  def xml_to_json(node: Node): JsonNode = {

    val txt = xml_writeAsString(node)

    xml_reader.readTree(txt)

  }

  def json_to_xml(json_tree: JsonNode, root: String = "root") = Try {
    val xml_txt = xml_writer.withRootName(root).writeValueAsString(json_tree)
    val bais = new ByteArrayInputStream(xml_txt.getBytes)
    val doc_xml = xml_builder.parse(bais)
    bais.close()
    doc_xml
  }

  def json_to_xml_string(json_tree: JsonNode, root: String) = Try {
    xml_writer.withRootName(root)
      .writeValueAsString(json_tree)
  }

}