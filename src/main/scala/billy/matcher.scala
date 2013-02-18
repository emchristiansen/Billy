package nebula

import scala.Int.int2double
import scala.reflect.runtime.universe
import scala.runtime.ZippedTraversable2.zippedTraversable2ToTraversable

import org.opencv.features2d.DMatch

import breeze.linalg.DenseMatrix
import nebula.Matcher.KendallTau
import nebula.Matcher.L0
import nebula.Matcher.L1
import nebula.Matcher.L2
import nebula.SortDescriptor.implicitIndexedSeq
import nebula.util.JSONUtil
import nebula.util.JSONUtil.AddClassName
import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat

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

  ///////////////////////////////////////////////////////////  

  object L0
  object L1
  object L2
  object KendallTau

  /**
   * Turn a distance on IndexedSeq[Int] into a distance on SortDescriptor.
   */
  private def lift: DescriptorDistance[IndexedSeq[Int]] => DescriptorDistance[SortDescriptor] =
    distance =>
      (left, right) => distance(left.toIndexedSeq, right.toIndexedSeq)

  /**
   * Turn a distance on IndexedSeq[A] to a distance on DenseMatrix[A].
   */
  def liftToMatrix[A](distance: DescriptorDistance[IndexedSeq[A]]): DescriptorDistance[DenseMatrix[A]] =
    (left, right) => distance(left.data.toIndexedSeq, right.data.toIndexedSeq)

  implicit def implicitMatcherL0[A](self: L0.type) =
    Matcher[IndexedSeq[A]](l0)
  implicit def implicitMatcherMatrixL0[A](self: L0.type) =
    Matcher[DenseMatrix[A]](liftToMatrix(l0))
  implicit def implicitMatcherSortL0(self: L0.type) =
    Matcher[SortDescriptor](lift(l0))

  implicit def implicitMatcherL1[A <% Double](self: L1.type) =
    Matcher[IndexedSeq[A]](l1)
  implicit def implicitMatcherMatrixL1[A <% Double](self: L1.type) =
    Matcher[DenseMatrix[A]](liftToMatrix[A](l1))
  implicit def implicitMatcherSortL1(self: L1.type) =
    Matcher[SortDescriptor](lift(l1))

  implicit def implicitMatcherL2[A <% Double](self: L2.type) =
    Matcher[IndexedSeq[A]](l2)
  implicit def implicitMatcherMatrixL2[A <% Double](self: L2.type) =
    Matcher[DenseMatrix[A]](liftToMatrix[A](l2))
  implicit def implicitMatcherSortL2(self: L2.type) =
    Matcher[SortDescriptor](lift(l2))

  implicit def implictMatcher(self: KendallTau.type) =
    Matcher[SortDescriptor](kendallTau)
}

///////////////////////////////////////////////////////////

trait SingleMatcher[F] extends Matcher[F] {
  override def doMatch = Matcher.applyIndividual(distance)
}

///////////////////////////////////////////////////////////

case class LogPolarMatcher[N <% Normalizer[DenseMatrix[Int], DenseMatrix[F2]], M <% Matcher[DenseMatrix[F2]], F2](
  normalizer: N,
  matcher: M,
  normalizeByOverlap: Boolean,
  rotationInvariant: Boolean,
  scaleSearchRadius: Int)

object LogPolarMatcher {
  implicit def logPolarMatcher2Matcher[N <% Normalizer[DenseMatrix[Int], DenseMatrix[F2]], M <% Matcher[DenseMatrix[F2]], F2](self: LogPolarMatcher[N, M, F2])(
    implicit ed: ((N, M)) => ExpectedDistance): Matcher[DenseMatrix[Int]] =
    Matcher(LogPolar.distance(self))
}

///////////////////////////////////////////////////////////

case class NormalizedMatcher[N <% Normalizer[F1, F2], M <% Matcher[F2], F1, F2](
  normalizer: N,
  matcher: M)

object NormalizedMatcher {
  // TODO: This implicit isn't picked up, probably a compiler bug.
  implicit def normalizedMatcher2Matcher[N <% Normalizer[F1, F2], M <% Matcher[F2], F1, F2](self: NormalizedMatcher[N, M, F1, F2]): Matcher[F1] =
    Matcher[F1]((left: F1, right: F1) => {
      val leftNormalized = self.normalizer.normalize(left)
      val rightNormalized = self.normalizer.normalize(right)
      self.matcher.distance(leftNormalized, rightNormalized)
    })

}

///////////////////////////////////////////////////////////

trait MatcherJsonProtocol extends DefaultJsonProtocol {
  implicit val matcherL0JsonProtocol = JSONUtil.singletonObject(Matcher.L0)
  implicit val matcherL1JsonProtocol = JSONUtil.singletonObject(Matcher.L1)
  implicit val matcherL2JsonProtocol = JSONUtil.singletonObject(Matcher.L2)
  implicit val matcherKendallTauJsonProtocol = JSONUtil.singletonObject(Matcher.KendallTau)

  ///////////////////////////////////////////////////////////

  implicit def logPolarMatcherJsonProtocol[N <% Normalizer[DenseMatrix[Int], DenseMatrix[F2]]: JsonFormat, M <% Matcher[DenseMatrix[F2]]: JsonFormat, F2] =
    jsonFormat5(LogPolarMatcher.apply[N, M, F2]).addClassInfo("LogPolarMatcher")

  ///////////////////////////////////////////////////////////

  implicit def normalizedMatcherJsonProtocol[N <% Normalizer[F1, F2]: JsonFormat, M <% Matcher[F2]: JsonFormat, F1, F2] =
    jsonFormat2(NormalizedMatcher.apply[N, M, F1, F2]).addClassInfo("NormalizedMatcher")
}

object MatcherJsonProtocol extends MatcherJsonProtocol