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

case class PatchExtractor(
  normalizeRotation: Boolean,
  normalizeScale: Boolean,
  patchWidth: Int,
  blurWidth: Int,
  color: String)

object PatchExtractor {
  implicit def PatchExtractor2Extractor(
    self: PatchExtractor): Extractor[IndexedSeq[Int]] =
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

trait PatchJsonProtocol extends DefaultJsonProtocol {
  implicit val patchExtractorJsonProtocol =
    jsonFormat5(PatchExtractor.apply).addClassInfo("PatchExtractor")
}

object PatchJsonProtocol extends PatchJsonProtocol