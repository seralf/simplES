package csv

import scala.collection.immutable.ListMap
import simples.utilities.ModelAdapter
import scala.io.Source
import scala.util.{ Try => TryExp }
import scala.util.Failure
import scala.util.Success

object CSVParser {

  def fromURL(url: String)(implicit delimiter: Char = '"', separator: Char = ',', encoding: String = "UTF-8") =
    new CSVParser(url, delimiter, separator, encoding)

}

/**
 * TODO: refactorization introducing InputStream!
 */
class CSVParser(url: String, val D: Char, val S: Char, encoding: String) {

  import scala.reflect._
  import scala.reflect.runtime.universe._

  val BOM = "\uFEFF"

  val RGX_SPLIT = s"""${S}(?=([^${D}]*"[^${D}]*${D})*[^${D}]*$$)"""

  def parse[T: TypeTag: ClassTag](): Seq[T] = {

    lazy val headers: Seq[String] = this.parseLines().head
      .map { item =>

        if (item.startsWith(BOM)) {
          System.err.println("WARNING: BOM FOUND!", item)
          item.substring(1)
        } else {
          item
        }

      }

    // TODO: handling exception by line
    this.parseLines().tail
      .map { line =>

        if (line.size != headers.size) System.err.println("WARNING: different sizes!")

        val map: Map[String, Any] = ListMap(headers.zip(line): _*).toMap
        ModelAdapter.fromMap(map)
      }

  }

  // TODO: refactorization
  def parseLines(): Seq[Seq[String]] = {

    using(Source.fromURL(url)(encoding)) { input =>
      input.getLines().toStream
    }(_.close())
      .map(splitLine)

  }

  private def splitLine(txt: String) = txt.split(RGX_SPLIT).toList

  // naive example
  private def using[T <: { def close() }, R](resource: T)(action: T => R)(rsc: T => Unit): R = {

    action(resource)

  }

}
