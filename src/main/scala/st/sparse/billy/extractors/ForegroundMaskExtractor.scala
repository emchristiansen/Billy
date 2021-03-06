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
import grizzled.math.stats
import st.sparse.sundry._
import java.io.File
import scala.slick.session.Database
import st.sparse.persistentmap._
import scala.pickling._

///////////////////////////////////////////////////////////

/**
 * Represents an extractor that blurs a region, possibly normalizes it up
 * to similarity, and then extracts a square patch of given size and color.
 */
case class ForegroundMaskExtractor(
  patchWidth: Int)(
    implicit matlabLibraryRoot: MatlabLibraryRoot,
    database: Database) extends ExtractorSeveral[DenseMatrix[Double]] with Logging {
  override def extract = (image, keyPoints) =>
    extractCorrect(image, keyPoints).map(_.map(RichDenseMatrix.inpaint))

  lazy val getBoundaries = Memo.connectElseCreateJson(
    "memo_MatlabGPbSegmenter_boundariesImageScaling",
    database,
    MatlabGPbSegmenter.boundariesImageScaling)

  /**
   * This method makes very clear which mask probabilities are unknown.
   */
  def extractCorrect = (image: Image, keyPoints: Seq[KeyPoint]) => {
    val boundaries = getBoundaries(image)
    //    // TODO
    //    val boundaries = {
    //      val cacheFile = new File(
    //        s"/home/eric/t/2013_q4/pilgrimOutput/boundaryScratch/${image.hashCode.abs}.png")
    //      if (!cacheFile.exists) {
    //        logger.debug("Boundary cache miss.")
    //        MatlabGPbSegmenter.boundariesImageScaling(image).write(cacheFile)
    //      } else logger.debug("Boundary cache hit.")
    //
    //      Image(cacheFile)
    //    }

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

