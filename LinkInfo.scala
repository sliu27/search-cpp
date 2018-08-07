package search.sol

class LinkInfo(val title: Int) {

  private var numLinksTo = 0;
  private var linksFrom: Set[Integer] = Set()

  def addLinkFrom(page: Int): LinkInfo = {
    if (page != title) {
      linksFrom += page
    }
    this
  }

  def getLinksFrom(): Set[Integer] = linksFrom

  def setLinksTo(num: Int): LinkInfo = {
    numLinksTo = num
    this
  }

  def getLinksTo(): Int = numLinksTo

  override def toString(): String =
    "numLinksTo: " + numLinksTo + ", " + "Linked to by: " + linksFrom

}

object LinkInfo {

  def main(args: Array[String]) {
    val z: LinkInfo = new LinkInfo(1)
    var x: Map[Int, String] = Map()
    x += (1 -> "a")
    x += (1 -> "b")
    println(x)
    var y: Array[Int] = new Array[Int](3)
    for (q <- y) println(q)
  }

}