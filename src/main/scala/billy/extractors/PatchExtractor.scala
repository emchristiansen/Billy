package billy.extractors

import nebula._
import nebula.imageProcessing._
import nebula.util._

import billy._

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
import com.sksamuel.scrimage._
import com.sksamuel.scrimage.filter._

///////////////////////////////////////////////////////////

/**
 * Represents an extractor that blurs a region, possibly normalizes it up
 * to similarity, and then extracts a square patch of given size and color.
 */
case class PatchExtractor[A: ColorEncoder](
  colorCoding: A,
  patchWidth: Int,
  blurWidth: Int) extends ExtractorSingle[ColorEncoder[A]#ColorCoding] {
  import PatchExtractor._

  override def extractSingle = (image: Image, keyPoint: KeyPoint) => {
    rawPixels(
      colorCoding,
      patchWidth,
      blurWidth)(image, keyPoint)
  }
}

object PatchExtractor {
  def rawPixels[A: ColorEncoder](
    colorCoding: A,
    patchWidth: Int,
    blurWidth: Int)(
      image: Image,
      keyPoint: KeyPoint): Option[ColorEncoder[A]#ColorCoding] = {

    val blurred = image.filter(GaussianBlurFilter(blurWidth))
    val patchOption: Option[Image] = ???
    //      blurred.extractPatch(patchWidth, keyPoint)
    for (
      patch <- patchOption
    ) yield {
      //      val values = Pixel.getPixelsOriginal(patch).flatMap(interpretColor(color))
      //      values
      ???
    }
  }
}

///**
// * Views to Extractor.
// */
//trait PatchExtractor2Extractor {
//  implicit def PatchExtractor2Extractor(
//    self: PatchExtractor): Extractor[IndexedSeq[Int]] =
//    Extractor(
//      (image: Image, keyPoint: KeyPoint) => {
//        Extractor.rawPixels(
//          self.normalizeRotation,
//          self.normalizeScale,
//          self.patchWidth,
//          self.blurWidth,
//          self.color)(image, keyPoint)
//      })
//}
//
//object PatchExtractor extends PatchExtractor2Extractor

//// TODO: Big hack, delete me!!!
//case class LUCIDExtractor(
//  normalizeRotation: Boolean,
//  normalizeScale: Boolean,
//  patchWidth: Int,
//  blurWidth: Int,
//  color: String)
//
///**
// * Views to Extractor.
// */
//trait LUCIDExtractor2Extractor {
//  implicit def LUCIDExtractor2Extractor(
//    self: LUCIDExtractor): Extractor[IndexedSeq[Int]] =
//    Extractor(
//      (image: Image, keyPoint: KeyPoint) => {
//        val rawPixelsOption = Extractor.rawPixels(
//          self.normalizeRotation,
//          self.normalizeScale,
//          self.patchWidth,
//          self.blurWidth,
//          self.color)(image, keyPoint)
//        for (rawPixels <- rawPixelsOption) yield {
//          val rank =
//            SortDescriptor.fromUnsorted(SortDescriptor.fromUnsorted(rawPixels))
//          rank.values
//        }
//      })
//}
//
//object LUCIDExtractor extends LUCIDExtractor2Extractor
