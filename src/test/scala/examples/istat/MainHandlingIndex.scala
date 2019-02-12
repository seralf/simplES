package examples.istat

import simples.ES
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest

object MainHandlingIndex extends App {

  val (_index, _type) = ("taxonomy", "cp2011")

  val es = ES.remote
  es.start().get

  println("\n\n\n\n")
  println(s"ES> drop index ${_index}")

  es.client.admin().indices().close(new CloseIndexRequest(_index)).get
  es.client.admin().indices().delete(new DeleteIndexRequest(_index)).get

  println(".........................\n\n\n\n")

  //  es.index.drop(_index) match {
  //    case Success(ok) => System.out.println(s"CP2011> drop index ${_index}")
  //    case Failure(ko) => System.err.println(s"CP2011> error dropping index ${_index}")
  //  }

}