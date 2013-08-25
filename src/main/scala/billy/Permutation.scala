package billy

////////////////////////////////////////////////

/**
 * Something which is guaranteed to be a permutation.
 */
case class Permutation(data: IndexedSeq[Int]) {
  require(data.sorted == (0 until data.size))

  def inverse = {
    val values = data.zipWithIndex.sortBy(_._1).map(_._2)
    Permutation(values)
  }

  def compose(that: Permutation) = {
    require(data.size == that.data.size)
    
    val values = for (t <- that.data) yield data(t)
    Permutation(values)
  }

  def numCycles = {
    val seen = collection.mutable.Set[Int]()
    var numCycles = 0
    for (start <- data) {
      numCycles += { if (seen.contains(start)) 0 else 1 }
      var current = data(start)
      while (current != start) {
        seen += current
        current = data(current)
      }
    }
    numCycles
  }
}