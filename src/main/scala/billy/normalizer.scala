package billy

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._

import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import grizzled.math.stats
import nebula.util._
import SortDescriptor._
import breeze.linalg._
import reflect._

///////////////////////////////////////////////////////////    

/**
 * Represents normalizations that may be applied to descriptors.
 */
trait Normalizer[-A, +B] {
  def normalize: A => B
}

/**
 * Normalizations that act on 1D and 2D arrays.
 */
trait PatchNormalizer {
  /**
   * The identity normalization.
   */
  object Raw

  /**
   * Rescales to zero mean and unit norm.
   */
  object NCC

  /**
   * Replaces an array of ordered elements with their rank permutation.
   */
  object Rank

  //  object Order
  //  object NormalizeRange
  //  object UniformRank
}

object PatchNormalizer extends PatchNormalizer with PatchNormalizerToNormalizer

///////////////////////////////////////////////////////////

/**
 * Implicit mappings from patch normalizers to Normalizer.
 */
trait PatchNormalizerToNormalizer extends PatchNormalizer {
  implicit class Raw2Normalizer[A](self: Raw.type) extends Normalizer[A, A] {
    override def normalize: A => A = identity
  }

  ///////////////////////////////////////////////////////////  

  implicit class NCC2NormalizerSeq[A <% Double](self: NCC.type) extends Normalizer[Seq[A], IndexedSeq[Double]] {
    override def normalize = data => {
      val doubleData = data.map(_.to[Double])
      val mean = MathUtil.mean(doubleData)
      val centered = data.toIndexedSeq.map(_ - mean)
      val norm = MathUtil.l2Norm(centered.toArray)
      // If the standard deviation is low, merely center the data.  
      if (norm < 0.001) centered
      else {
        val normalized = centered.map(_ / norm)
        assertNear(MathUtil.mean(normalized), 0)
        assertNear(MathUtil.l2Norm(normalized.toArray), 1)
        normalized
      }
    }
  }

  ///////////////////////////////////////////////////////////

  implicit class Rank2NormalizerSeq[A: Ordering](self: Rank.type) extends Normalizer[Seq[A], SortDescriptor] {
    override def normalize = data => SortDescriptor.fromUnsorted(SortDescriptor.fromUnsorted(data))
  }

  ///////////////////////////////////////////////////////////

  /**
   * Makes any Normalizer on 1D data to 1D data a normalizer on 2D data.
   */
  implicit class LiftSeq2DenseMatrix[N <% Normalizer[Seq[A], IndexedSeq[B]], A, B: ClassTag](normalizer: N) extends Normalizer[DenseMatrix[A], DenseMatrix[B]] {
    override def normalize = matrix => {
      val normalized = normalizer.normalize(matrix.data)
      new DenseMatrix(matrix.rows, normalized.toArray)
    }
  }

  // TODO: Scala bug?
  implicit def ncc2NormalizerDenseMatrix[A <% Double](self: NCC.type) = new LiftSeq2DenseMatrix[NCC.type, A, Double](self)

  /**
   * Makes any Normalizer on 1D data to SortDescriptor a normalizer on 2D data.
   */
  implicit class LiftSortDescriptor2DenseMatrix[N <% Normalizer[Seq[A], SortDescriptor], A](normalizer: N) extends Normalizer[DenseMatrix[A], DenseMatrix[Int]] {
    override def normalize = matrix => {
      val normalized = normalizer.normalize(matrix.data)
      new DenseMatrix(matrix.rows, normalized.toArray)
    }
  }

  ///////////////////////////////////////////////////////////

  //  implicit class RangeNormalizeDouble(self: NormalizeRange.type) extends Normalizer[IndexedSeq[Double], IndexedSeq[Double]] {
  //    // Sets the value range in [0, 255].
  //    override def normalize: IndexedSeq[Double] => IndexedSeq[Double] = data => {
  //      val min = data.min
  //      val range = data.max - min
  //      if (range == 0) data // Do nothing.
  //      else {
  //        val normalized = data.map(x => ((x - min) * 255.0 / range))
  //        assert(normalized.min == 0)
  //        assert(normalized.max == 255)
  //        normalized
  //      }
  //    }
  //  }
  //
  //  implicit class OrderNormalize[A: Ordering](self: Order.type) extends Normalizer[IndexedSeq[A], SortDescriptor] {
  //    override def normalize: IndexedSeq[A] => SortDescriptor = data => SortDescriptor.fromUnsorted(data)
  //  }
  //
  //  implicit class UniformRankNormalize[A: Ordering](self: UniformRank.type) extends Normalizer[IndexedSeq[A], IndexedSeq[Int]] {
  //    override def normalize: IndexedSeq[A] => IndexedSeq[Int] = data => {
  //      val distinctPixelValues = data.toSet.toList
  //      // TODO: Fix this
  //      val rank = RankNormalize(Rank).normalize(data).toArray
  //      for (value <- distinctPixelValues) {
  //        val indices = data.zipWithIndex.filter(_._1 == value).map(_._2)
  //        val meanRank = (indices.map(rank.apply).sum.toDouble / indices.size).round.toInt
  //        indices.foreach(i => rank(i) = meanRank)
  //      }
  //      rank.toIndexedSeq
  //    }
  //  }  
}
