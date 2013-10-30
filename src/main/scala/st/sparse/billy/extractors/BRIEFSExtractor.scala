package st.sparse.billy.extractors

import com.sksamuel.scrimage.filter.GaussianBlurFilter
import org.opencv.core.Point
import st.sparse.sundry._
import scala.util.Try
import com.sksamuel.scrimage.Image
import st.sparse.billy.Gray
import st.sparse.billy._
import com.sksamuel.scrimage.PixelTools

case class BRIEFSExtractor(
  numPairs: Int,
  patchWidth: Int,
  blurWidth: Int) extends ExtractorSingle[IndexedSeq[(Boolean, Option[Double], Option[Double])]] {
  require(numPairs > 0)
  require(patchWidth > 0)
  require(blurWidth > 0)

  val random = new util.Random(0)
  val pointPairs = numPairs times {
    def nextCoordinate() = random.nextDouble.abs % patchWidth

    val left = new Point(nextCoordinate(), nextCoordinate())
    val right = new Point(nextCoordinate(), nextCoordinate())
    (left, right)
  }

  override def extractSingle = (image, keyPoint) => {
    val rgbPatchOption = {
      val blurred = image.filter(GaussianBlurFilter(blurWidth))
      Try(blurred.subpixelSubimageCenteredAtPoint(
        keyPoint.pt.x,
        keyPoint.pt.y,
        patchWidth.toDouble / 2,
        patchWidth.toDouble / 2)).toOption
    }

    val boundariesPatchOption = {
      val boundaries = MatlabGPbSegmenter.boundariesImage(image)
      Try(boundaries.subpixelSubimageCenteredAtPoint(
        keyPoint.pt.x,
        keyPoint.pt.y,
        patchWidth.toDouble / 2,
        patchWidth.toDouble / 2)).toOption
    }

    for (
      rgbPatch <- rgbPatchOption;
      boundariesPatch <- boundariesPatchOption
    ) yield {
      val segmentation = Segmentation.fromBoundariesImage(boundariesPatch)
      val centerPoint = new Point(patchWidth / 2.0, patchWidth / 2.0)
      def probabilityFromCenterSegment(otherPoint: Point) =
        segmentation.probabilityInSameSegment(centerPoint, otherPoint)

      pointPairs map {
        case (leftPoint, rightPoint) =>
          val leftPixel = rgbPatch.subpixel(leftPoint.x, leftPoint.y)
          val rightPixel = rgbPatch.subpixel(rightPoint.x, rightPoint.y)

          (PixelTools.gray(leftPixel) < PixelTools.gray(rightPixel),
            probabilityFromCenterSegment(leftPoint),
            probabilityFromCenterSegment(rightPoint))
      }
    }
  }
}