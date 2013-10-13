package billy

import org.opencv.core.KeyPoint

import com.sksamuel.scrimage.Image

///////////////////////////////////////////////////////////

/** Finds keypoints in a given image.
 */
trait Detector {
  def detect: Image => Seq[KeyPoint]
}
