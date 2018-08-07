package search.sol

/**
 * A class for DocIDPair tuples
 */
class DocIDPair(val id: Int, var score: Double) {

  /**
   * Gets the ID
   */
  def getID: Int = this.id

  /**
   * Gets the score
   */
  def getScore: Double = this.score

  /**
   * Allows the score to be reset
   */
  def setScore(score: Double) {
    this.score = score
  }

  @Override
  override def toString: String = "id:" + id + " score: " + score

}

/**
 * Companion tester object for DocIDPair
 */
object DocIDPair {

  def main(args: Array[String]) {
    val d: DocIDPair = new DocIDPair(1, 3.2)
    println(d.getID == 1)
    println(d.getScore == 3.2)
    d.setScore(5.2)
    println(d.getScore == 5.2)
    println(d.toString == "id:1 score: 5.2")
  }

}