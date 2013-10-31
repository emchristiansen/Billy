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
import org.opencv.core.Point

///////////////////////////////////////////////////////////

/**
 * Represents an extractor that blurs a region, possibly normalizes it up
 * to similarity, and then extracts a square patch of given size and color.
 */
case class ForegroundMaskExtractor(
  patchWidth: Int) extends ExtractorSeveral[DenseMatrix[Option[Double]]] {
  override def extract = (image, keyPoints) => {
    val boundaries = MatlabGPbSegmenter.boundariesImageScaling(image)
    assert(boundaries.width == image.width)
    assert(boundaries.height == image.height)
    keyPoints map { keyPoint =>
      val boundariesPatchOption =
        Try(boundaries.subpixelSubimageCenteredAtPoint(
          keyPoint.pt.x,
          keyPoint.pt.y,
          patchWidth.toDouble / 2,
          patchWidth.toDouble / 2)).toOption
      for (boundariesPatch <- boundariesPatchOption) yield {
        val segmentation = Segmentation.fromBoundariesImage(boundariesPatch)
        val centerPoint = new Point(patchWidth / 2.0, patchWidth / 2.0)
        def probabilityFromCenterSegment(otherPoint: Point) =
          segmentation.probabilityInSameSegment(centerPoint, otherPoint)
          
        DenseMatrix.tabulate[Option[Double]](
            boundariesPatch.height, 
            boundariesPatch.width) {
          case (y, x) => probabilityFromCenterSegment(new Point(x, y))
        }
      }
    }
  }
}

