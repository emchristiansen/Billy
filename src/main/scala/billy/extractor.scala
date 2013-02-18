package billy

import java.awt.image.BufferedImage

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._
import billy.brown._
import billy.mpie._
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
import imageProcessing.ImageUtil
import imageProcessing.Pixel

import nebula.imageProcessing.RichImage.bufferedImage
import nebula.util.JSONUtil.AddClassName
import nebula.util.JSONUtil.singletonObject
import spray.json.DefaultJsonProtocol
import spray.json.JsonFormat
import util.OpenCVUtil
import util.Util
import nebula.util._

///////////////////////////////////////////////////////////

trait Extractor[F] {
  def extract: Extractor.ExtractorAction[F]

  def extractSingle: Extractor.ExtractorActionSingle[F]
}

///////////////////////////////////////////////////////////

object Extractor {
  type ExtractorAction[F] = (BufferedImage, Seq[KeyPoint]) => Seq[Option[F]]
  type ExtractorActionSingle[F] = (BufferedImage, KeyPoint) => Option[F]

  def fromAction[F](extractSeveral: ExtractorAction[F]) = new Extractor[F] {
    override def extract = extractSeveral

    override def extractSingle = (image, keyPoint) =>
      extract(image, Seq(keyPoint)).head
  }

  def applySeveral[F](extractSingle: ExtractorActionSingle[F]): ExtractorAction[F] =
    (image: BufferedImage, keyPoints: Seq[KeyPoint]) =>
      keyPoints.map(k => extractSingle(image, k))

  def apply[F](single: ExtractorActionSingle[F]): Extractor[F] = new Extractor[F] {
    override def extract = applySeveral(extractSingle)

    override def extractSingle = single
  }

  // TODO: These should be enums, not strings.
  def interpretColor(color: String)(pixel: Pixel): Seq[Int] = color match {
    case "Gray" => pixel.gray
    case "sRGB" => pixel.sRGB
    case "lRGB" => pixel.lRGB
    case "HSB" => pixel.hsb
    case "Lab" => pixel.lab
    case "XYZ" => pixel.xyz
    case _ => sys.error("Color not supported. Do you have a typo?")
  }

  def rawPixels(
    normalizeRotation: Boolean,
    normalizeScale: Boolean,
    patchWidth: Int,
    blurWidth: Int,
    color: String)(
      image: BufferedImage,
      keyPoint: KeyPoint): Option[IndexedSeq[Int]] = {
    // TODO
    asserty(!normalizeRotation)
    asserty(!normalizeScale)

    val blurred = ImageUtil.boxBlur(blurWidth, image)
    val patchOption = ImageUtil.extractPatch(blurred, patchWidth, keyPoint)
    for (
      patch <- patchOption
    ) yield {
      val values = Pixel.getPixelsOriginal(patch).flatMap(interpretColor(color))
      values
    }
  }

  def doubleExtractorFromEnum(enum: Int): ExtractorActionSingle[IndexedSeq[Double]] =
    (image: BufferedImage, keyPoint: KeyPoint) => {
      val extractor = DescriptorExtractor.create(enum)
      val imageMat = OpenCVUtil.bufferedImageToMat(image)
      val descriptor = new Mat
      extractor.compute(imageMat, new MatOfKeyPoint(keyPoint), descriptor)

      DenseMatrixUtil.matToMatrixDouble(descriptor) map (_.data.toIndexedSeq)
//      
//      if (descriptor.rows == 0 || descriptor.cols == 0) None
//      else {
//        asserty(descriptor.rows == 1)
//        asserty(descriptor.cols > 0)
//        //        asserty(descriptor.`type` == CvType.CV_8UC1)
//
//        val doubles = for (c <- 0 until descriptor.cols) yield {
//          val doubles = descriptor.get(0, c)
//          asserty(doubles.size == 1)
//          doubles.head
//        }
//
//        Some(doubles)
//      }
    }

  def intExtractorFromEnum(enum: Int): ExtractorActionSingle[IndexedSeq[Int]] = (image, keyPoint) => {
    val toInt: Option[IndexedSeq[Double]] => Option[IndexedSeq[Int]] =
      (seq) => seq.map(_.map(_.round.toInt))
    // TODO: Why doesn't the following work?
    //    toInt compose doubleExtractorFromEnum(enum)
    toInt(doubleExtractorFromEnum(enum)(image, keyPoint))
  }

  def booleanExtractorFromEnum(enum: Int): ExtractorActionSingle[IndexedSeq[Boolean]] = (image, keyPoint) => {
    val toBoolean: Option[IndexedSeq[Int]] => Option[IndexedSeq[Boolean]] =
      (seq) => seq.map(_.flatMap(Util.numToBits(8)))
    // TODO: Why doesn't the following work?      
    //    toBoolean compose intExtractorFromEnum(enum)
    toBoolean(intExtractorFromEnum(enum)(image, keyPoint))
  }
}

///////////////////////////////////////////////////////////

trait SingleExtractor[F] extends Extractor[F] {
  override def extract = Extractor.applySeveral(extractSingle)
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
    (image: BufferedImage, keyPoints: Seq[KeyPoint]) => {
      val unnormalized = normalizedExtractor.extractor.extract(image, keyPoints)
      unnormalized.map(_.map(normalizedExtractor.normalizer.normalize))
    })
}

///////////////////////////////////////////////////////////

trait ExtractorJsonProtocol extends DefaultJsonProtocol {
  implicit def normalizedExtractorJsonProtocol[E, N, F1, F2](
    implicit evExtractor: E => Extractor[F1],
    evNormalizer: N => Normalizer[F1, F2],
    evEJson: JsonFormat[E],
    evNJson: JsonFormat[N]) =
    jsonFormat2(NormalizedExtractor.apply[E, N, F1, F2])
}

object ExtractorJsonProtocol extends ExtractorJsonProtocol 