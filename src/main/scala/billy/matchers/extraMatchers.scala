package billy.matchers

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._

import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import scala.Int.int2double
import scala.reflect.runtime.universe
import scala.runtime.ZippedTraversable2.zippedTraversable2ToTraversable

import org.opencv.features2d.DMatch

import breeze.linalg.DenseMatrix
import billy.SortDescriptor.implicitIndexedSeq

///////////////////////////////////////////////////////////

sealed trait Matcher[F] {
  def doMatch: Matcher.MatcherAction[F]

  def distance: Matcher.DescriptorDistance[F]
}

object Matcher {
  type MatcherAction[F] = (Boolean, Seq[F], Seq[F]) => Seq[DMatch]
  type DescriptorDistance[F] = (F, F) => Double

  def applyIndividual[F](distanceMethod: DescriptorDistance[F]): MatcherAction[F] =
    (allPairs, leftDescriptors, rightDescriptors) => {
      if (allPairs) {
        for (
          (left, leftIndex) <- leftDescriptors.zipWithIndex.par;
          (right, rightIndex) <- rightDescriptors.zipWithIndex
        ) yield {
          val distance = distanceMethod(left, right)
          new DMatch(leftIndex, rightIndex, distance.toFloat)
        }
      } else {
        for (((left, right), index) <- leftDescriptors.zip(rightDescriptors).zipWithIndex.par) yield {
          val distance = distanceMethod(left, right)
          new DMatch(index, index, distance.toFloat)
        }
      }
    } toIndexedSeq

  def apply[F](descriptorDistance: DescriptorDistance[F]): Matcher[F] = new Matcher[F] {
    override def doMatch = applyIndividual(distance)

    override def distance = descriptorDistance
  }

  def l0(left: IndexedSeq[Any], right: IndexedSeq[Any]): Int =
    (left, right).zipped.count({ case (l, r) => l != r })

  def l1[A <% Double](left: IndexedSeq[A], right: IndexedSeq[A]): Double =
    (left, right).zipped.map({ case (l, r) => (l - r).abs }).sum

  def l2[A <% Double](left: IndexedSeq[A], right: IndexedSeq[A]): Double = {
    math.sqrt((left, right).zipped.map({ case (l, r) => math.pow(l - r, 2) }).sum)
  }

  def kendallTau(left: SortDescriptor, right: SortDescriptor): Int = {
    val size = left.size
    assert(size == right.size)
    val errors = for (i <- 0 until size; j <- i + 1 until size) yield {
      if (left(i) < left(j) == right(i) < right(j)) 0
      else 1
    }
    errors.sum
  }
}

///////////////////////////////////////////////////////////

trait SingleMatcher[F] extends Matcher[F] {
  override def doMatch = Matcher.applyIndividual(distance)
}

///////////////////////////////////////////////////////////

trait BatchMatcher[F] extends Matcher[F] {
  override def distance = (left, right) =>
    doMatch(true, Seq(left), Seq(right)).head.distance
}

///////////////////////////////////////////////////////////

case class NormalizedMatcher[N <% Normalizer[F1, F2], M <% Matcher[F2], F1, F2](
  normalizer: N,
  matcher: M)

object NormalizedMatcher {
  // TODO: This implicit isn't picked up, probably a compiler bug.
  implicit def normalizedMatcher2Matcher[N <% Normalizer[F1, F2], M <% Matcher[F2], F1, F2](
    self: NormalizedMatcher[N, M, F1, F2]): Matcher[F1] =
    Matcher[F1]((left: F1, right: F1) => {
      val leftNormalized = self.normalizer.normalize(left)
      val rightNormalized = self.normalizer.normalize(right)
      self.matcher.distance(leftNormalized, rightNormalized)
    })

}
