package billy.matchers

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import scala.Int.int2double
import scala.reflect.runtime.universe
import scala.runtime.ZippedTraversable2.zippedTraversable2ToTraversable

import org.opencv.features2d.DMatch

import breeze.linalg.DenseMatrix
import billy.SortDescriptor.implicitIndexedSeq
import nebula.util.JSONUtil
import nebula.util.JSONUtil.AddClassName
import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat

///////////////////////////////////////////////////////////

object VectorMatcher {
  import Matcher._

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

trait VectorMatcherJsonProtocol extends DefaultJsonProtocol {
  implicit val vectorMatcherL0JsonProtocol = 
    JSONUtil.singletonObject(VectorMatcher.L0)
  implicit val vectorMatcherL1JsonProtocol = 
    JSONUtil.singletonObject(VectorMatcher.L1)
  implicit val vectorMatcherL2JsonProtocol = 
    JSONUtil.singletonObject(VectorMatcher.L2)
  implicit val vectorMatcherKendallTauJsonProtocol = 
    JSONUtil.singletonObject(VectorMatcher.KendallTau)
}

object VectorMatcherJsonProtocol extends VectorMatcherJsonProtocol