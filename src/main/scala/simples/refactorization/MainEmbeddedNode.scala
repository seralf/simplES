package simples.refactorization

object MainEmbeddedNode extends App {

  val node = EmbeddedNode.fromConfigFile("src/main/resources/conf/es-local.conf")

  node.start()

  Thread.sleep(30000)

  node.stop()

}