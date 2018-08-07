package search.sol

import scala.collection.mutable.HashMap

/**
 * Testing the easily-unit-testable methods
 * of Query and Index
 */
object Tester {

  def main(args: Array[String]) {
    // Index
    var y: HashMap[String, Double] = HashMap.empty
    y.+=("a" -> 2)
    y.+=("b" -> 4)
    println(Index.insertWordInto("b", y) == 5)
    println(y.get("b").get == 5)
    println(Index.insertWordInto("c", y) == 1)
    println(y.get("c").get == 1)

    println(
      Index.regex("a a:colon A|bar i49140 a[fd]341 [[d4]]").deep ==
        Array("a", "a", "colon", "A", "bar", "i49140",
          "a", "fd", "341", "[[d4]]").deep)
    println(Index.stem("village") == "villag")
    println(Index.stem("consume") == "consum")
    println(Index.stem("a") == "a")
    println(Index.stem("computer") == "comput")

    // Query
    println(
      Query.stem(Array("village", "consume", "a", "computer")).deep ==
        Array("villag", "consum", "a", "comput").deep)
  }

}