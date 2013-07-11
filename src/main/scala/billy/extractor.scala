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

  def fromAction[F](extractSeveral: ExtractorAction[F]): Extractor[F] =
    new Extractor[F] {
      override def extract = extractSeveral

      override def extractSingle = (image, keyPoint) =>
        extract(image, Seq(keyPoint)).head
    }

  def applySeveral[F](
    extractSingle: ExtractorActionSingle[F]): ExtractorAction[F] =
    (image: Image, keyPoints: Seq[KeyPoint]) =>
      keyPoints.map(k => extractSingle(image, k))

  def apply[F](single: ExtractorActionSingle[F]): Extractor[F] =
    new Extractor[F] {
      override def extract = applySeveral(extractSingle)

      override def extractSingle = single
    }

  // TODO: These should be types, not strings.
  def interpretColor(color: String)(pixel: Int): Seq[Int] = color match {
    case "Gray" => PixelTools.gray(pixel)
//    case "sRGB" => pixel.sRGB
//    case "lRGB" => pixel.lRGB
//    case "HSB" => pixel.hsb
//    case "Lab" => pixel.lab
//    case "XYZ" => pixel.xyz
    case _ => sys.error("Color not supported. Do you have a typo?")
  }

  def rawPixels(
    normalizeRotation: Boolean,
    normalizeScale: Boolean,
    patchWidth: Int,
    blurWidth: Int,
    color: String)(
      image: Image,
      keyPoint: KeyPoint): Option[IndexedSeq[Int]] = {
    // TODO
    assert(!normalizeRotation)
    assert(!normalizeScale)

    val blurred = image.boxBlur(blurWidth)
    val patchOption = blurred.extractPatch(patchWidth, keyPoint)
    for (
      patch <- patchOption
    ) yield {
      val values = Pixel.getPixelsOriginal(patch).flatMap(interpretColor(color))
      values
    }
  }

  object OpenCVLock
  def doubleExtractorSeveralFromEnum(enum: Int): ExtractorAction[IndexedSeq[Double]] =
    (image: Image, keyPoints: Seq[KeyPoint]) => {
      val extractor = DescriptorExtractor.create(enum)
      val imageMat = OpenCVUtil.bufferedImageToMat(image)
      val descriptor = new Mat

      val markedKeyPointsMat = {
        val marked =
          for ((keyPoint, index) <- keyPoints.zipWithIndex) yield {
            new KeyPoint(
              keyPoint.pt.x.toFloat,
              keyPoint.pt.y.toFloat,
              keyPoint.size,
              keyPoint.angle,
              keyPoint.response,
              keyPoint.octave,
              index)
          }
        new MatOfKeyPoint(marked: _*)
      }

      // Apparent concurrency bug that causes random crashes. Sigh.
      //      OpenCVLock.synchronized {
      extractor.compute(
        imageMat,
        markedKeyPointsMat,
        descriptor)
      //      }

      val descriptorsOption =
        DenseMatrixUtil.matToMatrixDoubleSingleChannel(descriptor)
      if (!descriptorsOption.isDefined) keyPoints.size times None
      else {
        val descriptors = descriptorsOption.get.toSeqSeq
        val markedKeyPoints = markedKeyPointsMat.toArray

        assert(descriptors.size == markedKeyPoints.size)

        val descriptorOptions =
          Array[Option[IndexedSeq[Double]]](keyPoints.size times None: _*)
        for ((descriptor, keyPoint) <- descriptors.zip(markedKeyPoints)) {
          val index = keyPoint.class_id
          descriptorOptions(index) = Some(descriptor)
        }
        descriptorOptions
      }
    }

  def doubleExtractorFromEnum(enum: Int): ExtractorActionSingle[IndexedSeq[Double]] =
    (image: Image, keyPoint: KeyPoint) => {
      val extractor = DescriptorExtractor.create(enum)
      val imageMat = OpenCVUtil.bufferedImageToMat(image)
      val descriptor = new Mat
      extractor.compute(imageMat, new MatOfKeyPoint(keyPoint), descriptor)

      DenseMatrixUtil.matToMatrixDoubleSingleChannel(descriptor) map (_.data.toIndexedSeq)
      //      
      //      if (descriptor.rows == 0 || descriptor.cols == 0) None
      //      else {
      //        assert(descriptor.rows == 1)
      //        assert(descriptor.cols > 0)
      //        //        assert(descriptor.`type` == CvType.CV_8UC1)
      //
      //        val doubles = for (c <- 0 until descriptor.cols) yield {
      //          val doubles = descriptor.get(0, c)
      //          assert(doubles.size == 1)
      //          doubles.head
      //        }
      //
      //        Some(doubles)
      //      }
    }

  val toInt: Option[IndexedSeq[Double]] => Option[IndexedSeq[Int]] =
    (seq) => seq.map(_.map(_.round.toInt))

  def intExtractorFromEnum(enum: Int): ExtractorActionSingle[IndexedSeq[Int]] = (image, keyPoint) => {

    // TODO: Why doesn't the following work?
    //    toInt compose doubleExtractorFromEnum(enum)
    toInt(doubleExtractorFromEnum(enum)(image, keyPoint))
  }

  val toBoolean: Option[IndexedSeq[Int]] => Option[IndexedSeq[Boolean]] =
    (seq) => seq.map(_.flatMap(Util.numToBits(8)))

  def booleanExtractorFromEnum(enum: Int): ExtractorActionSingle[IndexedSeq[Boolean]] = (image, keyPoint) => {

    // TODO: Why doesn't the following work?      
    //    toBoolean compose intExtractorFromEnum(enum)
    toBoolean(intExtractorFromEnum(enum)(image, keyPoint))
  }

  def booleanExtractorSeveralFromEnum(enum: Int): ExtractorAction[IndexedSeq[Boolean]] = (image, keyPoints) => {
    val doubles = doubleExtractorSeveralFromEnum(enum)(image, keyPoints)
    (doubles map toInt) map toBoolean
  }
}

///////////////////////////////////////////////////////////

trait SingleExtractor[F] extends Extractor[F] {
  override def extract = Extractor.applySeveral(extractSingle)
}

///////////////////////////////////////////////////////////

trait BatchExtractor[F] extends Extractor[F] {
  override def extractSingle = (image, keyPoint) =>
    extract(image, Seq(keyPoint)).head
}

///////////////////////////////////////////////////////////

case class NormalizedExtractor[E, N, F1, F2](
  extractor: E,
  normalizer: N)(
    implicit evExtractor: E => Extractor[F1],
    evNormalizer: N => Normalizer[F1, F2])

object NormalizedExtractor {
  implicit def toExtractor[E, N, F1, F2](normalizedExtractor: NormalizedExtractor[E, N, F1, F2])(
    implicit evExtractor: E => Extractor[F1],
    evNormalizer: N => Normalizer[F1, F2]): Extractor[F2] = Extractor.fromAction(
    (image: Image, keyPoints: Seq[KeyPoint]) => {
      val unnormalized = normalizedExtractor.extractor.extract(image, keyPoints)
      unnormalized.map(_.map(normalizedExtractor.normalizer.normalize))
    })
}
