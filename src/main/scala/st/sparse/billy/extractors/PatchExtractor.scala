package st.sparse.billy.extractors

import st.sparse.billy._
import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint
import org.opencv.features2d.DescriptorExtractor
import org.opencv.core.KeyPoint
import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector
import com.sksamuel.scrimage._
import com.sksamuel.scrimage.filter._
import scala.util.Try

///////////////////////////////////////////////////////////

/**
 * Represents an extractor that blurs a region, possibly normalizes it up
 * to similarity, and then extracts a square patch of given size and color.
 */
case class PatchExtractor(
  color: Color,
  patchWidth: Int,
  blurWidth: Int) extends ExtractorSingle[DenseMatrix[IndexedSeq[Int]]] {
  override def extractSingle = (image: Image, keyPoint: KeyPoint) => {
    val blurred = image.filter(GaussianBlurFilter(blurWidth))
    val patchOption: Option[Image] =
      Try(blurred.subpixelSubimageCenteredAtPoint(
        keyPoint.pt.x,
        keyPoint.pt.y,
        patchWidth.toDouble / 2,
        patchWidth.toDouble / 2)).toOption
    for (
      patch <- patchOption
    ) yield {
      def extract(x: Int, y: Int): IndexedSeq[Int] = {
        val pixel = patch.pixel(x, y)
        color match {
          case Gray => IndexedSeq(PixelTools.gray(pixel))
          case RGB => IndexedSeq(
            PixelTools.red(pixel),
            PixelTools.green(pixel),
            PixelTools.blue(pixel))
        }
      }

      DenseMatrix.tabulate[IndexedSeq[Int]](patchWidth, patchWidth) {
        case (y, x) => extract(x, y)
      }
    }
  }
}

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
