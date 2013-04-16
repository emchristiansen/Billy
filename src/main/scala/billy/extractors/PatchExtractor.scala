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

/**
 * Represents an extractor that blurs a region, possibly normalizes it up
 * to similarity, and then extracts a square patch of given size and color.
 */
case class PatchExtractor(
  normalizeRotation: Boolean,
  normalizeScale: Boolean,
  patchWidth: Int,
  blurWidth: Int,
  color: String)

/**
 * Views to Extractor.
 */
trait PatchExtractor2Extractor {
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

/**
 * Implementations of JsonProtocol.
 */
trait PatchExtractorJsonProtocol extends DefaultJsonProtocol {
  implicit val patchExtractorJsonProtocol =
    jsonFormat5(PatchExtractor.apply).addClassInfo("PatchExtractor")
}

object PatchExtractor extends PatchExtractor2Extractor with PatchExtractorJsonProtocol

// TODO: Big hack, delete me!!!
case class LUCIDExtractor(
  normalizeRotation: Boolean,
  normalizeScale: Boolean,
  patchWidth: Int,
  blurWidth: Int,
  color: String)

/**
 * Views to Extractor.
 */
trait LUCIDExtractor2Extractor {
  implicit def LUCIDExtractor2Extractor(
    self: LUCIDExtractor): Extractor[IndexedSeq[Int]] =
    Extractor(
      (image: BufferedImage, keyPoint: KeyPoint) => {
        val rawPixelsOption = Extractor.rawPixels(
          self.normalizeRotation,
          self.normalizeScale,
          self.patchWidth,
          self.blurWidth,
          self.color)(image, keyPoint)
        for (rawPixels <- rawPixelsOption) yield {
          val rank =
            SortDescriptor.fromUnsorted(SortDescriptor.fromUnsorted(rawPixels))
          rank.values
        }
      })
}

/**
 * Implementations of JsonProtocol.
 */
trait LUCIDExtractorJsonProtocol extends DefaultJsonProtocol {
  implicit val LUCIDExtractorJsonProtocol =
    jsonFormat5(LUCIDExtractor.apply).addClassInfo("LUCIDExtractor")
}

object LUCIDExtractor extends LUCIDExtractor2Extractor with LUCIDExtractorJsonProtocol