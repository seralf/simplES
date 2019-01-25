package simples.utilities

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object JSON {

  val mapper = new ObjectMapper
  mapper.registerModule(DefaultScalaModule)

  val reader = mapper.reader()
  val writer = mapper.writerWithDefaultPrettyPrinter()

}