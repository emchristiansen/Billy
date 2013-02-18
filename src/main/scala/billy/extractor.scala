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

object OpenCVExtractor {
  object BRISK
  object FREAK
  object BRIEF
  object ORB
  object SIFT
  object SURF

  implicit def implicitExtractor(self: BRISK.type) =
    Extractor(Extractor.booleanExtractorFromEnum(DescriptorExtractor.BRISK))
  implicit def implicitExtractor(self: FREAK.type) =
    Extractor(Extractor.booleanExtractorFromEnum(DescriptorExtractor.FREAK))
  implicit def implicitExtractor(self: BRIEF.type) =
    Extractor(Extractor.booleanExtractorFromEnum(DescriptorExtractor.BRIEF))
  implicit def implicitExtractor(self: ORB.type) =
    Extractor(Extractor.booleanExtractorFromEnum(DescriptorExtractor.ORB))
  implicit def implicitExtractor(self: SIFT.type) =
    Extractor(Extractor.doubleExtractorFromEnum(DescriptorExtractor.SIFT))
  implicit def implicitExtractor(self: SURF.type) =
    Extractor(Extractor.doubleExtractorFromEnum(DescriptorExtractor.SURF))
}

case class PatchExtractor(
  normalizeRotation: Boolean,
  normalizeScale: Boolean,
  patchWidth: Int,
  blurWidth: Int,
  color: String)

object PatchExtractor {
  implicit def implicitPatchExtractor(self: PatchExtractor): Extractor[IndexedSeq[Int]] =
    Extractor(
      (image: BufferedImage, keyPoint: KeyPoint) => {
        Extractor.rawPixels(
          self.normalizeRotation,
          self.normalizeScale,
          self.patchWidth,
          self.blurWidth,
          self.color)(image, keyPoint)
      })
}

///////////////////////////////////////////////////////////

case class LogPolarExtractor(
  //  extractorType: PatchExtractorType.PatchExtractorType,
  steerScale: Boolean,
  //  partitionIntoRings: Boolean,
  minRadius: Double,
  maxRadius: Double,
  numScales: Int,
  numAngles: Int,
  blurWidth: Int,
  color: String)

object LogPolarExtractor {
  implicit def implicitLogPolarExtractor(self: LogPolarExtractor): Extractor[DenseMatrix[Int]] =
    new Extractor[DenseMatrix[Int]] {
      override def extract = (image: BufferedImage, keyPoints: Seq[KeyPoint]) => {
        asserty(self.color == "Gray")

        // TODO: Make scaleXangle not angleXscale
        LogPolar.rawLogPolarSeq(
          self.steerScale,
          self.minRadius,
          self.maxRadius,
          self.numScales,
          self.numAngles,
          self.blurWidth)(image, keyPoints)
      }

      override def extractSingle = (image: BufferedImage, keyPoint: KeyPoint) =>
        extract(image, Seq(keyPoint)).head
    }
}

///////////////////////////////////////////////////////////      

case class ELUCIDExtractor(
  normalizeRotation: Boolean,
  normalizeScale: Boolean,
  numSamplesPerRadius: Int,
  stepSize: Double,
  numRadii: Int,
  blurWidth: Int,
  color: String)

object ELUCIDExtractor {
  implicit def implicitELUCIDExtractor(self: ELUCIDExtractor): Extractor[SortDescriptor] =
    Extractor(
      (image: BufferedImage, keyPoint: KeyPoint) => {
        val numSamples = self.numRadii * self.numSamplesPerRadius + 1
        val radii = (1 to self.numRadii).map(_ * self.stepSize)
        val angles = (0 until self.numSamplesPerRadius).map(_ * 2 * math.Pi / self.numSamplesPerRadius)

        def samplePattern(scaleFactor: Double, rotationOffset: Double): Seq[DenseVector[Double]] = {
          requirey(scaleFactor > 0)
          Seq(DenseVector(0.0, 0.0)) ++ (for (
            angle <- angles;
            radius <- radii
          ) yield {
            val scaledRadius = scaleFactor * radius
            val offsetAngle = rotationOffset + angle
            DenseVector(scaledRadius * math.cos(offsetAngle), scaledRadius * math.sin(offsetAngle))
          })
        }

        def samplePoints(keyPoint: KeyPoint): Seq[DenseVector[Double]] = {
          val scaleFactor = if (self.normalizeScale) {
            asserty(keyPoint.size > 0)
            keyPoint.size / 10.0
          } else 1

          val rotationOffset = if (self.normalizeRotation) {
            asserty(keyPoint.angle != -1)
            keyPoint.angle * 2 * math.Pi / 360
          } else 0

          //    println(rotationOffset)

          samplePattern(scaleFactor, rotationOffset).map(_ + DenseVector(keyPoint.pt.x, keyPoint.pt.y))
        }

        val blurred = ImageUtil.boxBlur(self.blurWidth, image)

        val pointOptions = samplePoints(keyPoint).map(point => blurred.getSubPixel(point(0), point(1)))
        if (pointOptions.contains(None)) None
        else {
          val unsorted = pointOptions.flatten.flatMap(Extractor.interpretColor(self.color))
          Some(SortDescriptor.fromUnsorted(unsorted))
        }
      })
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
  implicit val openCVExtractorBriskJsonProtocol = singletonObject(OpenCVExtractor.BRISK)
  implicit val openCVExtractorFreakJsonProtocol = singletonObject(OpenCVExtractor.FREAK)
  implicit val openCVExtractorBriefJsonProtocol = singletonObject(OpenCVExtractor.BRIEF)
  implicit val openCVExtractorOrbJsonProtocol = singletonObject(OpenCVExtractor.ORB)
  implicit val openCVExtractorSiftJsonProtocol = singletonObject(OpenCVExtractor.SIFT)
  implicit val openCVExtractorSurfJsonProtocol = singletonObject(OpenCVExtractor.SURF)

  /////////////////////////////////////////////////////////

  implicit val patchExtractorJsonProtocol =
    jsonFormat5(PatchExtractor.apply).addClassInfo("PatchExtractor")

  /////////////////////////////////////////////////////////

  implicit val logPolarExtractorJsonProtocol =
    jsonFormat7(LogPolarExtractor.apply).addClassInfo("LogPolarExtractor")

  /////////////////////////////////////////////////////////

  implicit val elucidExtractorJsonProtocol =
    jsonFormat7(ELUCIDExtractor.apply).addClassInfo("ELUCIDExtractor")

  /////////////////////////////////////////////////////////    

  implicit def normalizedExtractorJsonProtocol[E, N, F1, F2](
    implicit evExtractor: E => Extractor[F1],
    evNormalizer: N => Normalizer[F1, F2],
    evEJson: JsonFormat[E],
    evNJson: JsonFormat[N]) =
    jsonFormat2(NormalizedExtractor.apply[E, N, F1, F2])
}

object ExtractorJsonProtocol extends ExtractorJsonProtocol 