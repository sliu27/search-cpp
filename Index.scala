package search.sol
import scala.xml.Node
import scala.xml.NodeSeq
import java.lang.Math
import scala.util.matching.Regex
import java.util.Arrays
import Array._
import java.io.BufferedWriter
import java.io.FileWriter
import scala.collection.mutable.HashMap
import search.src.PorterStemmer
import scala.collection.mutable.MutableList
import scala.collection.mutable.HashSet

/**
 *  Index
 */
class Index(val corpus: String, val titlef: String, val indexf: String) {

  import Index._

  private final val e = 0.15

  private val corpusNode: NodeSeq = xml.XML.loadFile(corpus) \ "page"

  private var titleMap: HashMap[String, Int] = HashMap.empty
  private val tWriter: BufferedWriter = new BufferedWriter(new FileWriter(titlef))
  private var id = 0
  // Initializing the titleMap: (title -> id)
  for (doc <- corpusNode) {
    val title: String = (doc \ "title").text.trim
    titleMap.+=(title -> id)
    tWriter.write(id + "#" + title)
    tWriter.newLine
    id += 1
  }
  tWriter.close

  private var numDocs = id
  private var tdfs: Array[HashMap[String, Double]] = new Array(numDocs)
  private var idfs: HashMap[String, Double] = HashMap.empty
  private var W: Array[Array[Double]] = Array.ofDim[Double](numDocs, numDocs)
  private var hatRankMap: Array[Double] = new Array(numDocs)

  /**
   * Actually indexes the documents (i.e. produces all the files)
   */
  def index {
    parse
    hatRank
    writeToFiles
  }

  /**
   * The bulk of indexer's work, including
   *   - Parsing and stemming all words
   *  - Factoring links
   *  - Calculating TDF and IDF
   *  - Calculating the weights matrix for Hatrank
   */
  private def parse {
    for (doc <- this.corpusNode) {
      val title = (doc \ "title").text.trim
      val text: String = (doc \ "text").text.trim + " " + title
      val currId = this.titleMap.get(title).get
      val initRegexValues: Array[String] = regex(text)

      var docTDFS: HashMap[String, Double] = HashMap.empty
      var linkTo: HashSet[Int] = HashSet.empty
      var currIDFS: HashSet[String] = HashSet.empty
      var runningMax = 1.0
      for (word <- initRegexValues) {
        if (word.charAt(0) != '[') {
          val wrd = stem(word)
          runningMax = Math.max(insertWordInto(wrd, docTDFS), runningMax)
          currIDFS.+=(wrd)
        } else {
          var linkText: String = word.substring(2, word.length - 2)
          var pageTo: Option[Int] = None
          if (word.contains('|')) {
            var linkSplit = linkText.split('|')
            pageTo = this.titleMap.get(linkSplit(0))
            if (linkSplit.length > 1) {
              var wordsInDoc = regex(linkSplit(linkSplit.length - 1))
              for (w <- wordsInDoc) {
                val wrd = stem(w)
                runningMax = Math.max(insertWordInto(w, docTDFS), runningMax)
                currIDFS.+=(w)
              }
            }
          } else {
            pageTo = this.titleMap.get(linkText)
            var wordsInDoc = regex(linkText)
            for (w <- wordsInDoc) {
              val wrd = stem(w)
              runningMax = Math.max(insertWordInto(wrd, docTDFS), runningMax)
              currIDFS.+=(wrd)
            }
          }
          if (pageTo != None && (pageTo.get != currId)) {
            linkTo.+=(pageTo.get)
          }
        }
        
      }
      for (k <- currIDFS) {
        insertWordInto(k, this.idfs)
      }
      normalizeTDF(docTDFS, runningMax)
      this.tdfs(currId) = docTDFS
      createWRow(linkTo, this.W(currId))
    }
  }

  /**
   *  Given a weights matrix already made by parse, computes
   *  Hatrank for all documents
   */
  private def hatRank {
    var r: Array[Double] = new Array(this.numDocs)
    var rPrime: Array[Double] = new Array(this.numDocs)
    for (j <- 0 to this.numDocs - 1) {
      r(j) = 0
      rPrime(j) = 1.0 / this.numDocs
    }
    while (distance(r, rPrime) > 0.0001) {
      for (i <- 0 to this.numDocs - 1) {
        r(i) = rPrime(i)
      }
      for (j <- 0 to this.numDocs - 1) {
        var score: Double = 0
        for (k <- 0 to this.numDocs - 1) {
          score = score + this.W(k)(j) * r(k)
        }
        rPrime(j) = score
      }
    }
    this.hatRankMap = rPrime
  }

  /**
   *  Writes the data to the files specified, normalizing
   *  idf in the process of doing so
   */
  private def writeToFiles {
    val iWriter: BufferedWriter = new BufferedWriter(new FileWriter(indexf))
    iWriter.write(this.numDocs + "\n")
    for (id <- 0 to this.numDocs - 1) {
      iWriter.write(id + "\n" + this.hatRankMap(id) + "\n")
      var currTDFS = this.tdfs(id)
      for (word <- currTDFS.keys) {
        var idfNormal = Math.log(this.numDocs / this.idfs.get(word).get)
        var combinedIDFTDF = idfNormal * currTDFS.get(word).get
        iWriter.write(word + " " + combinedIDFTDF + "\n")
      }
      iWriter.write("\n")
    }
    iWriter.close
  }

  /**
   *  Computes the Euclidean distance between two Hatrank iterations
   *
   *  @param prev - the previous iteration of Hatrank scores
   *  @param curr - the Hatrank scores just produced
   *  @return the Euclidean distance between prev and current
   */
  private def distance(prev: Array[Double], curr: Array[Double]): Double = {
    var sum: Double = 0
    for (k <- 0 to this.numDocs - 1) {
      var diff = curr(k) - prev(k)
      sum += (diff * diff)
    }
    Math.sqrt(sum)
  }

  /**
   *  Normalizes the TDF
   *
   *  @param docTDFS - the word count for each word in the document
   *  @param runningMax - the normalizing factor to divide by
   */
  private def normalizeTDF(docTDFS: HashMap[String, Double], runningMax: Double) {
    for (key <- docTDFS.keys) {
      docTDFS.update(key, docTDFS.get(key).get / runningMax)
    }
  }

  /**
   *  Creates a series of ascribed weight values for a particular index
   *
   *  @param linkTo - a set of all documents that a page J links to
   *  @parma currDoc - the array reference for j's weights ascribed matrix
   */
  private def createWRow(linkTo: HashSet[Int], currDoc: Array[Double]) {
    val score: Double = this.e / this.numDocs
    for (i <- 0 to this.numDocs - 1) {
      currDoc(i) = score
    }
    if (linkTo == HashSet.empty) {
      for (i <- 0 to this.numDocs - 1) {
        currDoc(i) = score + (1 - this.e) / this.numDocs
      }
    } else {
      for (j <- linkTo) {
        currDoc(j) = score + (1 - this.e) / linkTo.size
      }
    }
  }

}

/**
 *  The Indexer's singleton object
 */
object Index {
  /**
   *  Inserts a word into the TDF
   *
   *  @param key - the word to insert
   *  @param docTDFS - the hashMap of TDFS
   *  @return the count of the word just inserted
   */
  def insertWordInto(key: String, docTDFS: HashMap[String, Double]): Double = {
    val getWord: Option[Double] = docTDFS.get(key)
    var curr = 1.0
    getWord match {
      case Some(ct) =>
        docTDFS.update(key, ct + 1)
        curr = ct + 1
      case None => docTDFS.+=(key -> 1)
    }
    curr
  }

  /**
   *  Splits a string, keeping alphanumeric words,
   *  words with apostrophes, and
   *  links
   *
   *  @param words - the words to split
   *  @return an array split according to regex
   */
  def regex(words: String): Array[String] = {
    val reg: Regex = new Regex(
      """\[\[[^\[]+?\]\]|[^\W_]+’[^\W_]+|[^\W_]+""")
    reg.findAllMatchIn(words).toArray.map { aMatch => aMatch.matched }
  }

  /**
   *  Stems and lowercases a word
   *
   *  @param word - the input word
   *  @return the word, stemmed and lowercased
   */
  def stem(word: String): String =
    PorterStemmer.stem(word.toLowerCase)

  def main(args: Array[String]) {
    if (args.length != 3) {
      println("Invalid input!")
    } else {
      try {
        val i: Index = new Index(args(0), args(1), args(2))
        i.index
      } catch {
        case e: Exception => throw e
      }
    }
  }

}
