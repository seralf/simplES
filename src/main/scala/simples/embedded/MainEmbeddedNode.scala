package simples.embedded

object MainEmbeddedNode extends App {

  val node = EmbeddedNode.withConfigFile("src/main/resources/conf/es-embedded.conf")

  node.start()

  Thread.sleep(30000)

  node.stop()

}