package billy

import org.opencv.core.KeyPoint
import breeze.linalg._
import java.io.File
import st.sparse.sundry._
import org.apache.commons.io.FileUtils

///////////////////////////////////////

/**
 * Additional methods for KeyPoint.
 */
case class RichKeyPoint(keyPoint: KeyPoint) {
  /**
   * Create a vector from the (x, y) coordinates.
   */
  def toVector: DenseVector[Double] = DenseVector(keyPoint.pt.x, keyPoint.pt.y)

  /**
   * Returns the l2 distance between two keypoints.
   */
  def l2Distance(that: KeyPoint): Double =
    (toVector - RichKeyPoint(that).toVector).norm(2)
}

trait RichKeyPointImplicits {
  implicit def keyPoint2RichKeyPoint(keyPoint: KeyPoint) =
    RichKeyPoint(keyPoint)
}
