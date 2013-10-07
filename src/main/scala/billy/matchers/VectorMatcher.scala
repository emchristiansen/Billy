package billy.matchers

import breeze.linalg._
import billy._
import scala.reflect.ClassTag

///////////////////////////////////////////////////////////

/**
 * Represents distances on vectors or vector-like objects.
 */
object VectorMatcher {
  /**
   * The l0 distance, defined on any vector.
   */
  object L0

  /**
   * The l1 distance, defined on vectors of numeric types.
   */
  object L1

  /**
   * The l2 distance, defined on vectors of numeric types.
   */
  object L2

  /**
   * The Kendall tau distance, defined on vectors of numeric types.
   *
   * See http://en.wikipedia.org/wiki/Kendall_tau_rank_correlation_coefficient.
   */
  object KendallTau

  implicit def permutationToIndexedSeq(
    permutation: Permutation): IndexedSeq[Int] = permutation.data

  implicit def denseMatrixSeqToIndexedSeq[A: ClassTag](
    denseMatrix: DenseMatrix[IndexedSeq[A]]): IndexedSeq[A] =
    denseMatrix.data.flatten.toIndexedSeq

  // TODO: This code can be substantially simplified once this compiler bug
  // is fixed:
  // http://stackoverflow.com/questions/18902228/possible-bug-with-scala-2-10-2-implicits
  def l0Distance[A](left: IndexedSeq[A], right: IndexedSeq[A]): Int =
    (left, right).zipped count {
      case (l, r) => l != r
    }

  implicit def l0ToMatcher[A](l0: L0.type) = new MatcherSingle[IndexedSeq[A]] {
    override def distance = l0Distance[A]
  }

  implicit def l0ToMatcherMatrixSeq[A: ClassTag](l0: L0.type) =
    new MatcherSingle[DenseMatrix[IndexedSeq[A]]] {
      override def distance = (left, right) =>
        l0Distance(left: IndexedSeq[A], right: IndexedSeq[A])
    }

  //  private type DescriptorDistance[F] = (F, F) => Double
  //
  //  /**
  //   * Turn a distance on IndexedSeq[Int] into a distance on SortDescriptor.
  //   */
  //  private def lift: DescriptorDistance[IndexedSeq[Int]] => DescriptorDistance[Permutation] =
  //    distance => (left, right) => distance(left.data, right.data)
  //
  //  /**
  //   * Turn a distance on IndexedSeq[A] to a distance on DenseMatrix[A].
  //   */
  //  def liftToMatrix[A](
  //    distance: DescriptorDistance[IndexedSeq[A]]): DescriptorDistance[DenseMatrix[A]] =
  //    (left, right) => distance(left.data.toIndexedSeq, right.data.toIndexedSeq)
  //
  //  /**
  //   * Turn a distance on IndexedSeq[A] to a distance on
  //   * DenseMatrix[IndexedSeq[A]].
  //   */
  //  // TODO: Replace with implicit views to IndexedSeq.
  //  def liftToMatrixIndexedSeq[A](
  //    distance: DescriptorDistance[IndexedSeq[A]]): DescriptorDistance[DenseMatrix[IndexedSeq[A]]] =
  //    (left, right) =>
  //      distance(left.data.toIndexedSeq.flatten, right.data.toIndexedSeq.flatten)

  //  def l0[A](left: IndexedSeq[A], right: IndexedSeq[A]): Int = {
  //    require(left.size == right.size)
  //
  //    (left, right).zipped.count({ case (l, r) => l != r })
  //  }
  //
  //  def l1[A <% Double](left: IndexedSeq[A], right: IndexedSeq[A]): Double = {
  //    require(left.size == right.size)
  //
  //    (left, right).zipped.map({ case (l, r) => (l - r).abs }).sum
  //  }
  //
  //  def l2[A <% Double](left: IndexedSeq[A], right: IndexedSeq[A]): Double = {
  //    require(left.size == right.size)
  //
  //    math.sqrt((left, right).zipped.map {
  //      case (l, r) => math.pow(l - r, 2)
  //    }.sum)
  //  }
  //
  //  def kendallTau[A: Ordering](
  //    left: IndexedSeq[A],
  //    right: IndexedSeq[A]): Int = {
  //    require(left.size == right.size)
  //
  //    val discordances = for (
  //      i <- 0 until left.size;
  //      j <- i + 1 until left.size
  //    ) yield {
  //      def lt = implicitly[Ordering[A]].lt _
  //
  //      lt(left(i), left(j)) == lt(right(i), right(j))
  //    }
  //
  //    discordances count (identity)
  //  }
  //
  //  implicit class L02MatcherIndexedSeq[A](
  //    self: L0.type) extends MatcherSingle[IndexedSeq[A]] {
  //    override def distance = l0[A]
  //  }
  //  implicit class L02MatcherDenseMatrix[A](
  //    self: L0.type) extends MatcherSingle[DenseMatrix[A]] {
  //    override def distance = liftToMatrix(l0[A])
  //  }
  //  implicit class L02MatcherDenseMatrixIndexedSeq[A](
  //    self: L0.type) extends MatcherSingle[DenseMatrix[IndexedSeq[A]]] {
  //    override def distance = liftToMatrixIndexedSeq(l0[A])
  //  }
  //
  //  implicit class L12MatcherIndexedSeq[A <% Double](
  //    self: L1.type) extends MatcherSingle[IndexedSeq[A]] {
  //    override def distance = l1
  //  }
  //  implicit class L12MatcherDenseMatrix[A <% Double](
  //    self: L1.type) extends MatcherSingle[DenseMatrix[A]] {
  //    override def distance = liftToMatrix(l1[A])
  //  }
  //  implicit class L12MatcherDenseMatrixIndexedSeq[A <% Double](
  //    self: L1.type) extends MatcherSingle[DenseMatrix[IndexedSeq[A]]] {
  //    override def distance = liftToMatrixIndexedSeq(l1[A])
  //  }
  //
  //  implicit class L22MatcherIndexedSeq[A <% Double](
  //    self: L2.type) extends MatcherSingle[IndexedSeq[A]] {
  //    override def distance = l2
  //  }
  //  implicit class L22MatcherDenseMatrix[A <% Double](
  //    self: L2.type) extends MatcherSingle[DenseMatrix[A]] {
  //    override def distance = liftToMatrix(l2[A])
  //  }
  //  implicit class L22MatcherDenseMatrixIndexedSeq[A <% Double](
  //    self: L2.type) extends MatcherSingle[DenseMatrix[IndexedSeq[A]]] {
  //    override def distance = liftToMatrixIndexedSeq(l2[A])
  //  }
  //
  //  implicit class KendallTau2MatcherIndexedSeq(
  //    self: KendallTau.type) extends MatcherSingle[Permutation] {
  //    override def distance = (left, right) => kendallTau(left.data, right.data)
  //  }

  //  implicit def implicitMatcherMatrixL0[A](self: L0.type) =
  //    Matcher[DenseMatrix[A]](liftToMatrix(l0))
  //  implicit def implicitMatcherSortL0(self: L0.type) =
  //    Matcher[SortDescriptor](lift(l0))
  //
  //  implicit def implicitMatcherL1[A <% Double](self: VectorMatcher.L1.type) =
  //    Matcher[IndexedSeq[A]](l1)
  //  implicit def implicitMatcherMatrixL1[A <% Double](self: VectorMatcher.L1.type) =
  //    Matcher[DenseMatrix[A]](liftToMatrix[A](l1))
  //  implicit def implicitMatcherSortL1(self: VectorMatcher.L1.type) =
  //    Matcher[SortDescriptor](lift(l1))
  //
  //  implicit def implicitMatcherL2[A <% Double](self: VectorMatcher.L2.type) =
  //    Matcher[IndexedSeq[A]](l2)
  //  implicit def implicitMatcherMatrixL2[A <% Double](self: VectorMatcher.L2.type) =
  //    Matcher[DenseMatrix[A]](liftToMatrix[A](l2))
  //  implicit def implicitMatcherSortL2(self: VectorMatcher.L2.type) =
  //    Matcher[SortDescriptor](lift(l2))
  //
  //  implicit def implictMatcher(self: VectorMatcher.KendallTau.type) =
  //    Matcher[SortDescriptor](kendallTau)
}

//object VectorMatcher {
//  object L1
//
//  implicit class L12VectorMatcher[D: Numeric](
//    l1: L1.type) extends MatcherSingle[IndexedSeq[D]] {
//    override def distance = (left, right) =>
//      (left, right).zipped map {
//        case (l, r) => implicitly[Numeric[D]].minus(l, r).abs
//        //      case (l, r) => ((l: Double) - (r: Double)).abs
//      }
//  }
//
//  //  object L0 extends MatcherSingle[IndexedSeq[Boolean]] {
//  //    override def distance = (left, right) => (left, right).zipped count {
//  //      case (l, r) => l != r
//  //    }
//  //  }
//  //  
//  //  object L0 extends MatcherSingle[IndexedSeq[Int]] {
//  //    override def distance = (left, right) => (left, right).zipped count {
//  //      case (l, r) => l != r
//  //    }
//  //  }
//}

///**
// * Represents distances on vectors or vector-like objects.
// */
//trait VectorMatcher {
//  object L0
//  object L1
//  object L2
//  object KendallTau
//}
//
///**
// * Views to Matcher.
// */
//trait VectorMatcher2Matcher {
//  import Matcher._
//  
//  /**
//   * Turn a distance on IndexedSeq[Int] into a distance on SortDescriptor.
//   */
//  private def lift: DescriptorDistance[IndexedSeq[Int]] => DescriptorDistance[SortDescriptor] =
//    distance =>
//      (left, right) => distance(left.toIndexedSeq, right.toIndexedSeq)
//
//  /**
//   * Turn a distance on IndexedSeq[A] to a distance on DenseMatrix[A].
//   */
//  def liftToMatrix[A](
//      distance: DescriptorDistance[IndexedSeq[A]]): DescriptorDistance[DenseMatrix[A]] =
//    (left, right) => distance(left.data.toIndexedSeq, right.data.toIndexedSeq)
//
//  implicit def implicitMatcherL0[A](self: VectorMatcher.L0.type) =
//    Matcher[IndexedSeq[A]](l0)
//  implicit def implicitMatcherMatrixL0[A](self: VectorMatcher.L0.type) =
//    Matcher[DenseMatrix[A]](liftToMatrix(l0))
//  implicit def implicitMatcherSortL0(self: VectorMatcher.L0.type) =
//    Matcher[SortDescriptor](lift(l0))
//
//  implicit def implicitMatcherL1[A <% Double](self: VectorMatcher.L1.type) =
//    Matcher[IndexedSeq[A]](l1)
//  implicit def implicitMatcherMatrixL1[A <% Double](self: VectorMatcher.L1.type) =
//    Matcher[DenseMatrix[A]](liftToMatrix[A](l1))
//  implicit def implicitMatcherSortL1(self: VectorMatcher.L1.type) =
//    Matcher[SortDescriptor](lift(l1))
//
//  implicit def implicitMatcherL2[A <% Double](self: VectorMatcher.L2.type) =
//    Matcher[IndexedSeq[A]](l2)
//  implicit def implicitMatcherMatrixL2[A <% Double](self: VectorMatcher.L2.type) =
//    Matcher[DenseMatrix[A]](liftToMatrix[A](l2))
//  implicit def implicitMatcherSortL2(self: VectorMatcher.L2.type) =
//    Matcher[SortDescriptor](lift(l2))
//
//  implicit def implictMatcher(self: VectorMatcher.KendallTau.type) =
//    Matcher[SortDescriptor](kendallTau)
//}
//
//object VectorMatcher extends VectorMatcher with VectorMatcher2Matcher
