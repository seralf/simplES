package examples.lucene

import java.io.IOException

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.ParseException
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TopDocs
import org.apache.lucene.store.Directory
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.analysis.StopwordAnalyzerBase
import simples.JSON

object LuceneHelloWorld extends App {

  //New index
  var standardAnalyzer = new StandardAnalyzer()
  var directory = new RAMDirectory()
  var config = new IndexWriterConfig(standardAnalyzer)
  //Create a writer
  var writer = new IndexWriter(directory, config)
  var document = new Document()

  document.add(new TextField("content", "Hello World", Field.Store.YES))
  writer.addDocument(document)
  document.add(new TextField("content", "Hello people", Field.Store.YES))
  writer.addDocument(document)
  writer.close()

  // //Now let's try to search for Hello
  var reader = DirectoryReader.open(directory)
  var searcher = new IndexSearcher(reader)
  var parser = new QueryParser("content", standardAnalyzer)
  var query = parser.parse("Hello")
  var results = searcher.search(query, 5)
  println("Hits for Hello -->" + results.totalHits)
  //
  //case insensitive search
  query = parser.parse("hello")
  results = searcher.search(query, 5)
  println("Hits for hello -->" + results.totalHits)

  //search for a value not indexed
  query = parser.parse("Hi there")
  results = searcher.search(query, 5)
  println("Hits for Hi there -->" + results.totalHits)

}
