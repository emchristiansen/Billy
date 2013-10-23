package st.sparse.billy.experiments.wideBaseline

import st.sparse.billy._
import st.sparse.billy.experiments._
import java.io.File
import st.sparse.sundry._
import com.sksamuel.scrimage._
import scala.pickling._
import binary._
import st.sparse.billy.internal._
import breeze.linalg._
import org.opencv.core.KeyPoint
import thirdparty.jhlabs.image.PixelUtils

///////////////////////////////////////////////////////////

/**
 * Represents disparities for rectified stereo images.
 *
 * A disparity is an per-pixel x-coordinate offset between the two images.
 */
case class StereoDisparity(
  data: DenseMatrix[Option[Double]]) extends CorrespondenceMap with Logging {

  override def transformXYOnly(in: KeyPoint): Option[KeyPoint] = {
    // We don't have inter-pixel offsets, so we round here.
    // TODO: Do linear interpolation here.
    val xIndex = in.pt.x.round.toInt
    val yIndex = in.pt.y.round.toInt

    for (xOffset <- data(yIndex, xIndex)) yield {
      new KeyPoint(
        (xIndex + xOffset).toFloat,
        in.pt.y.toFloat,
        0)
    }
  }
}

object StereoDisparity {
  /**
   * Creates a `StereoDisparity` from a grayscale image where each pixel
   * represents x-coordinate offsets.
   *
   * The format is briefly described here:
   * http://vision.middlebury.edu/stereo/data/scenes2001/.
   * Note the dataset authors are inconsistent when choosing scale factors
   * for the disparity values; we are going to assume the scale
   * factor is 1, which works for the 2006 dataset:
   * http://vision.middlebury.edu/stereo/data/scenes2006/
   */
  def fromImage(image: Image): StereoDisparity = {
    val data = for (y <- 0 until image.height) yield {
      for (x <- 0 until image.width) yield {
        val gray = PixelTools.gray(image.pixel(x, y))
        // They make zero a special value meaning that the disparity
        // is unknown.
        // Loving the quality engineering.
        if (gray == 0) None
        else {
          Some(-gray.toDouble)
        }
      }
    }
    StereoDisparity(data.toDenseMatrix)
  }
}