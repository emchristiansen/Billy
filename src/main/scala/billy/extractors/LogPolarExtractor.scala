package billy.extractors

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
  implicit def LogPolarExtractor2Extractor(
    self: LogPolarExtractor): Extractor[DenseMatrix[Int]] =
    new Extractor[DenseMatrix[Int]] {
      override def extract = (
        image: BufferedImage,
        keyPoints: Seq[KeyPoint]) => {
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

trait LogPolarExtractorJsonProtocol extends DefaultJsonProtocol {
  implicit val logPolarExtractorJsonProtocol =
    jsonFormat7(LogPolarExtractor.apply).addClassInfo("LogPolarExtractor")
}

object LogPolarExtractorJsonProtocol extends LogPolarExtractorJsonProtocol