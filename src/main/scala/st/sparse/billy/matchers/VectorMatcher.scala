package st.sparse.billy.matchers

import breeze.linalg._
import st.sparse.billy._
import scala.reflect.ClassTag

import spire.algebra._
import spire.math._
import spire.implicits._

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
  def l0Distance[A](left: IndexedSeq[A], right: IndexedSeq[A]): Int = {
    require(left.size == right.size)
    (left, right).zipped count {
      case (l, r) => l != r
    }
  }

  implicit def l0ToMatcher[A](l0: L0.type) = new MatcherSingle[IndexedSeq[A]] {
    override def distance = l0Distance[A]
  }

  implicit def l0ToMatcherMatrixSeq[A: ClassTag](l0: L0.type) =
    new MatcherSingle[DenseMatrix[IndexedSeq[A]]] {
      override def distance = (left, right) =>
        l0Distance(left, right)
    }

  def l1Distance[A <% Double](
    left: IndexedSeq[A],
    right: IndexedSeq[A]): Double = {
    require(left.size == right.size)
    (left, right).zipped.map({ case (l, r) => (l - r).abs }).sum
  }

  implicit def l1ToMatcher[A <% Double](l1: L1.type) =
    new MatcherSingle[IndexedSeq[A]] {
      override def distance = l1Distance[A]
    }

  implicit def l1ToMatcherMatrixSeq[A <% Double: ClassTag](l1: L1.type) =
    new MatcherSingle[DenseMatrix[IndexedSeq[A]]] {
      override def distance = (left, right) =>
        l1Distance(left, right)
    }

  def l2Distance[A <% Double](left: IndexedSeq[A], right: IndexedSeq[A]): Double = {
    require(left.size == right.size)
    math.sqrt((left, right).zipped.map {
      case (l, r) => math.pow(l - r, 2)
    }.sum)
  }

  implicit def l2ToMatcher[A <% Double](l2: L2.type) =
    new MatcherSingle[IndexedSeq[A]] {
      override def distance = l2Distance[A]
    }

  implicit def l2ToMatcherMatrixSeq[A <% Double: ClassTag](l2: L2.type) =
    new MatcherSingle[DenseMatrix[IndexedSeq[A]]] {
      override def distance = (left, right) =>
        l2Distance(left, right)
    }

  def kendallTauDistance[A: Ordering](
    left: IndexedSeq[A],
    right: IndexedSeq[A]): Int = {
    require(left.size == right.size)

    val discordances = for (
      i <- 0 until left.size;
      j <- i + 1 until left.size
    ) yield {
      def lt = implicitly[Ordering[A]].lt _

      lt(left(i), left(j)) == lt(right(i), right(j))
    }

    discordances count (identity)
  }

  implicit def kendallTauToMatcher[A: Ordering](kendallTau: KendallTau.type) =
    new MatcherSingle[IndexedSeq[A]] {
      override def distance = kendallTauDistance[A]
    }

  implicit def kendallTauToMatcherMatrixSeq[A: Ordering: ClassTag](
    kendallTau: KendallTau.type) =
    new MatcherSingle[DenseMatrix[IndexedSeq[A]]] {
      override def distance = (left, right) =>
        kendallTauDistance(left, right)
    }
}
