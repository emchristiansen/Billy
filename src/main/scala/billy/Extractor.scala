package billy

import nebula._
import com.sksamuel.scrimage._
import nebula.util._

import billy._
import billy.brown._

import billy.smallBaseline._
import billy.wideBaseline._
import billy.summary._

import nebula._
import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.DescriptorExtractor
import org.opencv.features2d.KeyPoint

import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector

import util.OpenCVUtil
import util.Util
import nebula.util._
import nebula.util.DenseMatrixUtil._

///////////////////////////////////////////////////////////

trait Extractor[F] {
  def extract: Extractor.ExtractorAction[F]

  def extractSingle: Extractor.ExtractorActionSingle[F]
}

///////////////////////////////////////////////////////////

object Extractor {
  type ExtractorAction[F] = (Image, Seq[KeyPoint]) => Seq[Option[F]]
  type ExtractorActionSingle[F] = (Image, KeyPoint) => Option[F]
}
