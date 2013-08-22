package billy

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
}
