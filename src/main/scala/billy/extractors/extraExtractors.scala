package billy.extractors

import nebula._
import com.sksamuel.scrimage._
import nebula.util._

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
import billy._




/////////////////////////////////////////////////////////////
//
//case class NormalizedExtractor[E, N, F1, F2](
//  extractor: E,
//  normalizer: N)(
//    implicit evExtractor: E => Extractor[F1],
//    evNormalizer: N => Normalizer[F1, F2])
//
//object NormalizedExtractor {
//  implicit def toExtractor[E, N, F1, F2](normalizedExtractor: NormalizedExtractor[E, N, F1, F2])(
//    implicit evExtractor: E => Extractor[F1],
//    evNormalizer: N => Normalizer[F1, F2]): Extractor[F2] = Extractor.fromAction(
//    (image: Image, keyPoints: Seq[KeyPoint]) => {
//      val unnormalized = normalizedExtractor.extractor.extract(image, keyPoints)
//      unnormalized.map(_.map(normalizedExtractor.normalizer.normalize))
//    })
//}
