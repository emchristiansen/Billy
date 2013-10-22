package st.sparse.billy.experiments.wideBaseline

import org.opencv.core.KeyPoint
import breeze.linalg._
import st.sparse.sundry._
import org.apache.commons.io.FileUtils
import st.sparse.billy.internal._
import scala.Array.canBuildFrom
import st.sparse.billy.CorrespondenceMap
import st.sparse.sundry.Box.boxAToA

///////////////////////////////////////

case class Homography(matrix: DenseMatrix[Double]) extends CorrespondenceMap with Logging {
  require(matrix.rows == 3)
  require(matrix.cols == 3)

  /**
   * Transforms a 2D vector by the homography, and returns a 2D vector.
   */
  def transform(in: DenseVector[Double]): DenseVector[Double] = {
    require(in.size == 2)

    val inHomogeneous = DenseVector.vertcat(in, DenseVector[Double](1))
    val outHomogeneous = matrix * inHomogeneous

    logger.debug(s"in: $in")
    logger.debug(s"inHomogeneous: $inHomogeneous")
    logger.debug(s"matrix: $matrix")
    logger.debug(s"outHomogeneous: $outHomogeneous")
    
    outHomogeneous / outHomogeneous(2)
  }

  /**
   * Transforms a KeyPoint, considering only the (x, y) coordinates.
   * 
   * The returned KeyPoint has defaults for all other values.
   */
  override def transformXYOnly(in: KeyPoint): KeyPoint = {
    val inVector = DenseVector[Double](in.pt.x, in.pt.y)
    val outVector = transform(inVector)
    logger.debug(s"outVector: $outVector")
    new KeyPoint(
      outVector(0).toFloat,
      outVector(1).toFloat,
      0)
  }
}

object Homography {
  def fromFile(file: ExistingFile): Homography = {
    val lines = FileUtils.readFileToString(file).split("\n").filter(_.size > 0)
    val tokens = lines.map(_.split("[ \t]").filter(_.size > 0))
    val values = tokens.map(_.map(_.toDouble).toArray).toArray
    
    // DenseMatrix stores data in column-major order.
    val data = new DenseMatrix[Double](3, values.flatten).t
    Homography(data)
  }
}
