package search.sol

import java.io.BufferedReader
import java.io.FileReader
import java.io.InputStreamReader
import search.src.PorterStemmer
import java.io.IOException
import java.util.Arrays
import Array._
import scala.collection.mutable.HashMap
import scala.collection.mutable.MutableList

/**
 * A class to handle the query aspect of Search
 *
 * @param titlef - the file containing title id maps
 * @param indexf - the file containing word/PR info
 */
class Query(val titlef: String, val indexf: String) {

  import Query._

  private val ind: BufferedReader = new BufferedReader(new FileReader(indexf))
  private val numDocs = ind.readLine.toInt
  private var titleMap: Array[String] = new Array(numDocs);
  private var wordsMap: Array[HashMap[String, Double]] = new Array(numDocs);
  private var hatRankMap: Array[Double] = new Array(numDocs);
  makeTitleMap
  makeWordsAndHRMap

  /**
   * Makes the title map (i.e. id -> title) using an Array
   */
  private def makeTitleMap {
    val b: BufferedReader = new BufferedReader(new FileReader(titlef))
    var line = b.readLine
    while (line != null) {
      val idTitle: Array[String] = line.split('#')
      this.titleMap(idTitle(0).toInt) = idTitle(1)
      line = b.readLine
    }
    b.close
  }

  /**
   * Makes the word score and hat rank maps
   */
  private def makeWordsAndHRMap {
    var line = this.ind.readLine
    while (line != null) {
      var currMap: HashMap[String, Double] = HashMap.empty
      var id = line.toInt
      line = this.ind.readLine
      this.hatRankMap(id) = line.toDouble
      line = this.ind.readLine
      while (line != "") {
        val wordScore: Array[String] = line.split(' ')
        currMap.+=(wordScore(0) -> wordScore(1).toDouble)
        line = this.ind.readLine
      }
      this.wordsMap(id) = currMap
      line = this.ind.readLine
    }
  }

  /**
   * Gets the top 10 queries, based on the actual user inputs as
   * well as if the user wants pageRank or not
   *
   * @param query - the actual user input
   * @param ifPageRank - if the user wants pageRank
   */
  def getTopTen(query: Array[String], ifPageRank: Boolean) {
    var opt: List[DocIDPair] = Nil
    var size = 0
    for (i <- 0 to this.numDocs - 1) {
      val currMap: HashMap[String, Double] = this.wordsMap(i)
      var docScore: Double = 0
      var quer: Array[String] = stem(query)
      var occurred = false;
      for (wrd <- quer) {
        var score: Option[Double] = currMap.get(wrd)
        score match {
          case Some(scr) =>
            docScore += scr
            occurred = true
          case None =>
        }
      }
      if (occurred) {
        size += 1
        ifPageRank match {
          case true =>
            opt = opt.::(new DocIDPair(i, docScore * this.hatRankMap(i)))
          case false =>
            opt = opt.::(new DocIDPair(i, docScore))
        }
        if (size > 10) {
          opt = opt.sortWith((a: DocIDPair, b: DocIDPair) => a.getScore < b.getScore)
          opt = opt.tail
        }
      }
    }
    opt = opt.sortWith((a: DocIDPair, b: DocIDPair) => a.getScore > b.getScore)
    var toPrint: List[String] = opt.take(10).map { x => this.titleMap(x.getID) }
    var i = 1;
    for (doc <- toPrint) {
      println(i + " " + doc)
      i += 1
    }
  }

  /**
   * The steps to process the query
   *
   * @param query - the input
   * @param ifPageRank - if the user wants pageRank
   */
  def processQuery(query: Array[String], ifPageRank: Boolean) {
    getTopTen(query, ifPageRank)
  }
}

/**
 * The query object
 */
object Query {

  /**
   * Stemming an array of words (used to stem user input)
   */
  def stem(words: Array[String]): Array[String] =
    words.map { word => Index.stem(word.toLowerCase) }

  def main(args: Array[String]) {
    val reader: BufferedReader =
      new BufferedReader(new InputStreamReader(System.in));
    var doPageRank: Boolean = false
    var q: Query = null
    if (args.length < 2 || args.length > 3) {
      System.out.println("Insufficient files")
    } else if (args.length == 2) {
      try {
        q = new Query(args(0), args(1))
      } catch {
        case e: Exception =>
      }
    } else {
      q = new Query(args(1), args(2))
      if (args(0) == "--pagerank") {
        doPageRank = true;
        println("Running with PageRank!")
      }
    }
    println("search> ")
    var ipt = reader.readLine
    if (q != null) {
      while (ipt != null && ipt != ":quit") {
        var strgs = ipt.trim.split("\\s+")
        q.processQuery(strgs, doPageRank)
        println("search> ")
        ipt = reader.readLine
      }
      println("Thanks for searching!")
    } else {
      println("Bad file format!")
    }
  }

}