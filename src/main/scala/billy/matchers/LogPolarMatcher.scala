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

trait LogPolarMatcherJsonProtocol extends DefaultJsonProtocol {
  implicit def logPolarMatcherJsonProtocol[N <% Normalizer[DenseMatrix[Int], DenseMatrix[F2]]: JsonFormat, M <% Matcher[DenseMatrix[F2]]: JsonFormat, F2] =
    jsonFormat5(LogPolarMatcher.apply[N, M, F2]).addClassInfo("LogPolarMatcher")
}

object LogPolarMatcherJsonProtocol extends LogPolarMatcherJsonProtocol