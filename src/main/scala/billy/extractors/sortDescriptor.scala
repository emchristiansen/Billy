//package billy
//
//
//import billy._
//import billy.brown._
//
//import billy.smallBaseline._
//import billy.wideBaseline._
//import billy.summary._
//
/////////////////////////////////////////////////////////////
// TODO
//trait PermutationLike[A] {
//  def invert: A
//  def compose(otherPermutation: A): A
//  def numCycles: Int
//}
//
/////////////////////////////////////////////////////////////
//
//case class SortDescriptor(values: IndexedSeq[Int]) {
//  assert(values.sorted == (0 until values.size))
//}
//
//object SortDescriptor {
//  def fromUnsorted[A: Ordering](values: Seq[A]): SortDescriptor = {
//    val permutation = values.zipWithIndex.sortBy(_._1).map(_._2)
//    SortDescriptor(permutation.toIndexedSeq)
//  }
//
//  implicit def implicitIndexedSeq(self: SortDescriptor): IndexedSeq[Int] = 
//    self.values
//
//  implicit def sortDescriptor(self: SortDescriptor) =
//    new PermutationLike[SortDescriptor] {
//      override def invert = {
//        val values = self.zipWithIndex.sortBy(_._1).map(_._2)
//        SortDescriptor(values)
//      }
//
//      override def compose(that: SortDescriptor) = {
//        val values = for (t <- that) yield self(t)
//        SortDescriptor(values)
//      }
//
//      override def numCycles = {
//        val seen = collection.mutable.Set[Int]()
//        var numCycles = 0
//        for (start <- self) {
//          numCycles += { if (seen.contains(start)) 0 else 1 }
//          var current = self(start)
//          while (current != start) {
//            seen += current
//            current = self(current)
//          }
//        }
//        numCycles
//      }
//    }
//}
//
//
//
//
//
